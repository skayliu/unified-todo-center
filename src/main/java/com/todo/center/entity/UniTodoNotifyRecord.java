package com.todo.center.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 待办通知推送记录实体
 */
@Data
@Entity
@Table(name = "uni_todo_notify_record")
public class UniTodoNotifyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String todoId;

    /**
     * 通知渠道：feishu/dingtalk/wechat/email/sms
     */
    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false, length = 64)
    private String receiverId;

    @Column(length = 64)
    private String templateCode;

    @Column(nullable = false)
    private Date sendTime = new Date();

    /**
     * 发送状态：0待发送 1发送成功 2发送失败
     */
    @Column(nullable = false)
    private Integer sendStatus = 0;

    @Column(length = 1024)
    private String failReason;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private Date nextRetryTime;

    @Column(length = 128)
    private String messageId;

    /**
     * 推送耗时(毫秒)
     */
    private Integer costTime;
}
