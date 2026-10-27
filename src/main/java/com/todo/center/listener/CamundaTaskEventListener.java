package com.todo.center.listener;

import cn.hutool.core.util.IdUtil;
import com.todo.center.entity.UniTodoMain;
import com.todo.center.repository.UniTodoMainRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Camunda任务事件监听器
 * 自动监听引擎任务创建/完成/删除事件，同步到统一待办表
 */
@Slf4j
@Component
public class CamundaTaskEventListener implements TaskListener {

    @Resource
    private UniTodoMainRepository todoMainRepository;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        String taskId = delegateTask.getId();
        log.debug("收到Camunda任务事件: event={}, taskId={}", eventName, taskId);

        switch (eventName) {
            case "create" -> handleTaskCreate(delegateTask);
            case "complete", "delete" -> handleTaskComplete(delegateTask);
            case "assignment" -> handleTaskAssignment(delegateTask);
        }
    }

    private void handleTaskCreate(DelegateTask task) {
        String assignee = task.getAssignee();
        if (assignee == null || assignee.isBlank()) {
            return;
        }
        // 幂等校验
        boolean exists = todoMainRepository.existsByAppIdAndBizTypeAndBizIdAndReceiverId(
                "camunda-bpm", task.getTaskDefinitionKey(), task.getId(), assignee
        );
        if (exists) {
            return;
        }

        UniTodoMain todo = new UniTodoMain();
        todo.setTodoId("CAM-" + IdUtil.fastSimpleUUID());
        todo.setAppId("camunda-bpm");
        todo.setBizType(task.getTaskDefinitionKey());
        todo.setBizId(task.getProcessInstanceId());
        todo.setReceiverId(assignee);
        todo.setInitiatorId((String) task.getVariable("initiator"));
        todo.setTitle(task.getName());
        todo.setPriority(getPriorityFromCamunda(task.getPriority()));
        todo.setStatus(1);
        todo.setCreateTime(new Date());
        todo.setDueDate(task.getDueDate());
        todo.setFormUrl("/camunda/app/tasklist/default/#/task/" + task.getId());
        todo.setCamundaTaskId(task.getId());
        todo.setCamundaProcessInstanceId(task.getProcessInstanceId());
        todo.setCreateBy("camunda-engine");
        todo.setUpdateBy("camunda-engine");
        todoMainRepository.save(todo);
        log.info("Camunda任务创建，自动同步待办: taskId={}, assignee={}", task.getId(), assignee);
    }

    private void handleTaskComplete(DelegateTask task) {
        todoMainRepository.findByCamundaTaskId(task.getId()).ifPresent(todo -> {
            todo.setStatus(3);
            todo.setCompleteTime(new Date());
            todo.setUpdateTime(new Date());
            todoMainRepository.save(todo);
            log.info("Camunda任务完成，自动更新待办状态: todoId={}", todo.getTodoId());
        });
    }

    private void handleTaskAssignment(DelegateTask task) {
        // 任务转派时更新待办处理人
        todoMainRepository.findByCamundaTaskId(task.getId()).ifPresent(todo -> {
            todo.setReceiverId(task.getAssignee());
            todo.setUpdateTime(new Date());
            todoMainRepository.save(todo);
            log.info("Camunda任务转派，更新待办处理人: todoId={}, newReceiver={}",
                    todo.getTodoId(), task.getAssignee());
        });
    }

    /**
     * Camunda优先级 0-100 转换为 统一待办1-3级
     */
    private Integer getPriorityFromCamunda(int camundaPriority) {
        if (camundaPriority >= 80) return 3; // 紧急
        if (camundaPriority >= 50) return 2; // 重要
        return 1; // 普通
    }
}
