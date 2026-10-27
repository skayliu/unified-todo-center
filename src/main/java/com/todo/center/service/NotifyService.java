package com.todo.center.service;

import com.todo.center.entity.UniTodoMain;
import com.todo.center.entity.UniTodoNotifyRecord;
import com.todo.center.repository.UniTodoNotifyRecordRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 多渠道通知服务
 */
@Slf4j
@Service
public class NotifyService {

    @Resource
    private UniTodoNotifyRecordRepository notifyRecordRepository;

    /**
     * 异步发送待办通知
     */
    @Async
    public void asyncSendTodoNotify(UniTodoMain todo) {
        // 默认发送到企业微信渠道
        sendNotify(todo, "wechat");
        // 紧急待办额外发送短信
        if (todo.getPriority() == 3) {
            sendNotify(todo, "sms");
        }
    }

    /**
     * 发送SLA升级告警通知
     */
    @Async
    public void sendSlaEscalateNotify(UniTodoMain todo, String escalateUser, int level) {
        log.info("发送SLA升级通知: todoId={}, escalateTo={}, level={}", todo.getTodoId(), escalateUser, level);
        // 这里实现飞书/钉钉/企微消息推送逻辑
        UniTodoNotifyRecord record = new UniTodoNotifyRecord();
        record.setTodoId(todo.getTodoId());
        record.setChannel("wechat");
        record.setReceiverId(escalateUser);
        record.setTemplateCode("SLA_ESCALATE");
        record.setSendTime(new Date());
        record.setSendStatus(1);
        record.setCostTime(50);
        notifyRecordRepository.save(record);
    }

    private void sendNotify(UniTodoMain todo, String channel) {
        long start = System.currentTimeMillis();
        UniTodoNotifyRecord record = new UniTodoNotifyRecord();
        record.setTodoId(todo.getTodoId());
        record.setChannel(channel);
        record.setReceiverId(todo.getReceiverId());
        record.setTemplateCode("TODO_NEW");
        record.setSendTime(new Date());

        try {
            // 实际场景调用对应渠道的SDK发送消息
            Thread.sleep(50); // 模拟调用耗时
            record.setSendStatus(1);
            record.setMessageId("MSG-" + System.currentTimeMillis());
            record.setCostTime((int)(System.currentTimeMillis() - start));
            log.info("消息发送成功: todoId={}, channel={}", todo.getTodoId(), channel);
        } catch (Exception e) {
            record.setSendStatus(2);
            record.setFailReason(e.getMessage());
            record.setRetryCount(0);
            record.setNextRetryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5分钟后重试
            log.error("消息发送失败: todoId={}, channel={}, error={}", todo.getTodoId(), channel, e.getMessage());
        }
        notifyRecordRepository.save(record);
    }
}
