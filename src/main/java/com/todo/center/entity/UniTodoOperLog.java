package com.todo.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;

/**
 * 待办操作审计日志实体
 */
@Data
@Entity
@Table(name = "uni_todo_oper_log")
public class UniTodoOperLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String todoId;

    /**
     * 操作类型：create/complete/transfer/cancel/escalate
     */
    @Column(nullable = false, length = 32)
    private String operType;

    @Column(nullable = false, length = 64)
    private String operUserId;

    @Column(nullable = false)
    private Date operTime = new Date();

    @Column(columnDefinition = "text")
    private String operContent;

    @Column(length = 64)
    private String fromUserId;

    @Column(length = 64)
    private String toUserId;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;
}
