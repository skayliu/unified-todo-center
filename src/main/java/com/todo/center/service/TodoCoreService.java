package com.todo.center.service;

import cn.hutool.core.util.IdUtil;
import com.todo.center.entity.UniTodoMain;
import com.todo.center.entity.UniTodoOperLog;
import com.todo.center.repository.UniTodoMainRepository;
import com.todo.center.repository.UniTodoOperLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一待办核心服务
 */
@Slf4j
@Service
public class TodoCoreService {

    @Resource
    private UniTodoMainRepository todoMainRepository;

    @Resource
    private UniTodoOperLogRepository operLogRepository;

    @Resource
    private TaskService taskService;

    @Resource
    private NotifyService notifyService;

    /**
     * 外部系统待办同步，内置幂等校验
     */
    @Transactional(rollbackFor = Exception.class)
    public String syncOuterTodo(UniTodoMain todo, String operator) {
        // 四元组幂等校验
        boolean exists = todoMainRepository.existsByAppIdAndBizTypeAndBizIdAndReceiverId(
                todo.getAppId(), todo.getBizType(), todo.getBizId(), todo.getReceiverId()
        );
        if (exists) {
            log.info("待办已存在, 幂等跳过: appId={}, bizId={}, receiver={}",
                    todo.getAppId(), todo.getBizId(), todo.getReceiverId());
            return "DUPLICATE";
        }

        todo.setTodoId("TODO-" + IdUtil.fastSimpleUUID());
        todo.setStatus(1);
        todo.setCreateTime(new Date());
        todo.setUpdateTime(new Date());
        todo.setCreateBy(operator);
        todo.setUpdateBy(operator);
        todoMainRepository.save(todo);

        // 记录操作日志
        saveOperLog(todo.getTodoId(), "create", operator, "外部系统同步创建待办", null, todo.getReceiverId(), null, null);

        // 异步发送通知
        notifyService.asyncSendTodoNotify(todo);
        log.info("外部待办同步成功: todoId={}", todo.getTodoId());
        return "SUCCESS";
    }

    /**
     * 完成待办核心逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTodo(String todoId, String userId, HashMap<String, Object> variables, String comment, String ip) {
        UniTodoMain todo = todoMainRepository.findByTodoId(todoId)
                .orElseThrow(() -> new RuntimeException("待办不存在: " + todoId));

        // 权限校验
        if (!todo.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权限操作该待办");
        }
        if (todo.getStatus() != 1) {
            throw new RuntimeException("待办状态非法，无法重复操作");
        }

        // 如果是Camunda原生待办，调用引擎API完成
        if (todo.getCamundaTaskId() != null) {
            Task camundaTask = taskService.createTaskQuery().taskId(todo.getCamundaTaskId()).singleResult();
            if (camundaTask != null) {
                if (comment != null && !comment.isBlank()) {
                    taskService.addComment(todo.getCamundaTaskId(), todo.getCamundaProcessInstanceId(), comment);
                }
                taskService.complete(todo.getCamundaTaskId(), variables);
            }
        }

        // 更新待办状态
        todo.setStatus(3);
        todo.setCompleteTime(new Date());
        todo.setUpdateTime(new Date());
        todo.setUpdateBy(userId);
        todoMainRepository.save(todo);

        // 记录操作日志
        saveOperLog(todoId, "complete", userId, comment, null, null, ip, null);

        // 异步回调源系统状态回写
        asyncCallbackSourceSystem(todo);

        log.info("待办处理完成: todoId={}, operator={}", todoId, userId);
    }

    /**
     * 查询用户待办列表
     */
    public List<UniTodoMain> queryUserPendingTodo(String userId) {
        // 先查外部系统待办
        List<UniTodoMain> todoList = todoMainRepository.findByReceiverIdAndStatusOrderByCreateTimeDesc(userId, 1);
        // 再查Camunda原生任务，同步到待办表
        List<Task> camundaTasks = taskService.createTaskQuery()
                .assignee(userId)
                .active()
                .list();
        // 自动同步Camunda任务到统一待办表
        syncCamundaTasksToTodo(camundaTasks, userId);
        return todoList;
    }

    /**
     * 批量完成待办
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchComplete(List<String> todoIds, String userId, String comment, String ip) {
        int successCount = 0;
        int failCount = 0;
        for (String todoId : todoIds) {
            try {
                completeTodo(todoId, userId, new HashMap<>(), comment, ip);
                successCount++;
            } catch (Exception e) {
                log.error("批量处理待办失败: todoId={}, error={}", todoId, e.getMessage());
                failCount++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("total", todoIds.size());
        return result;
    }

    /**
     * 同步Camunda任务到待办表
     */
    @Transactional
    public void syncCamundaTasksToTodo(List<Task> tasks, String userId) {
        for (Task task : tasks) {
            boolean exists = todoMainRepository.existsByAppIdAndBizTypeAndBizIdAndReceiverId(
                    "camunda-bpm", task.getTaskDefinitionKey(), task.getId(), userId
            );
            if (!exists) {
                UniTodoMain todo = new UniTodoMain();
                todo.setTodoId("CAM-" + IdUtil.fastSimpleUUID());
                todo.setAppId("camunda-bpm");
                todo.setBizType(task.getTaskDefinitionKey());
                todo.setBizId(task.getProcessInstanceId());
                todo.setReceiverId(userId);
                todo.setInitiatorId(task.getAssignee());
                todo.setTitle(task.getName());
                todo.setPriority(1);
                todo.setStatus(1);
                todo.setCreateTime(task.getCreateTime());
                todo.setDueDate(task.getDueDate());
                todo.setFormUrl("/camunda/app/tasklist/default/#/task/" + task.getId());
                todo.setCamundaTaskId(task.getId());
                todo.setCamundaProcessInstanceId(task.getProcessInstanceId());
                todo.setCreateBy("system");
                todo.setUpdateBy("system");
                todoMainRepository.save(todo);
                log.debug("自动同步Camunda任务到待办: taskId={}, assignee={}", task.getId(), userId);
            }
        }
    }

    @Async
    public void asyncCallbackSourceSystem(UniTodoMain todo) {
        // 实际场景调用源系统callbackUrl做状态回写，内置重试机制
        log.info("异步回调源系统: appId={}, bizId={}", todo.getAppId(), todo.getBizId());
    }

    private void saveOperLog(String todoId, String operType, String operUserId, String content,
                              String fromUserId, String toUserId, String ip, String ua) {
        UniTodoOperLog log = new UniTodoOperLog();
        log.setTodoId(todoId);
        log.setOperType(operType);
        log.setOperUserId(operUserId);
        log.setOperContent(content);
        log.setFromUserId(fromUserId);
        log.setToUserId(toUserId);
        log.setIpAddress(ip);
        log.setUserAgent(ua);
        operLogRepository.save(log);
    }
}
