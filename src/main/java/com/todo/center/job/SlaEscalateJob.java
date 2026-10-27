package com.todo.center.job;

import com.todo.center.entity.UniTodoMain;
import com.todo.center.repository.UniTodoMainRepository;
import com.todo.center.service.NotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * SLA超时扫描升级定时任务
 */
@Slf4j
@Component
public class SlaEscalateJob {

    @Resource
    private UniTodoMainRepository todoMainRepository;

    @Resource
    private NotifyService notifyService;

    /**
     * 每5分钟执行一次超时扫描
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60000)
    public void scanTimeoutTodo() {
        log.debug("开始扫描SLA超时待办...");
        // 查询所有已过截止时间、未触发SLA告警的待处理待办
        List<UniTodoMain> timeoutTodoList = todoMainRepository
                .findByDueDateBeforeAndStatusAndIsSlaWarn(new Date(), 1, 0);

        for (UniTodoMain todo : timeoutTodoList) {
            try {
                // 标记已触发SLA告警
                todo.setIsSlaWarn(1);
                todo.setSlaEscalateLevel(1);
                todoMainRepository.save(todo);

                // 默认升级到直接上级（实际项目中从组织架构获取）
                String escalateUser = todo.getReceiverId() + "_manager";
                notifyService.sendSlaEscalateNotify(todo, escalateUser, 1);

                log.info("SLA超时待办升级: todoId={}, title={}, escalateTo={}",
                        todo.getTodoId(), todo.getTitle(), escalateUser);
            } catch (Exception e) {
                log.error("处理SLA升级异常: todoId={}, error={}", todo.getTodoId(), e.getMessage());
            }
        }
        log.debug("SLA扫描完成，处理超时待办数量: {}", timeoutTodoList.size());
    }
}
