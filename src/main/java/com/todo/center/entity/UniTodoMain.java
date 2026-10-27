package com.todo.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;

/**
 * 统一待办主表实体
 */
@Data
@Entity
@Table(name = "uni_todo_main")
@Where(clause = "deleted = 0")
public class UniTodoMain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 待办全局唯一ID
     */
    @Column(nullable = false, length = 64, unique = true)
    private String todoId;

    /**
     * 源系统标识
     */
    @Column(nullable = false, length = 64)
    private String appId;

    /**
     * 业务类型
     */
    @Column(nullable = false, length = 64)
    private String bizType;

    /**
     * 源系统业务ID
     */
    @Column(nullable = false, length = 64)
    private String bizId;

    /**
     * 处理人账号
     */
    @Column(nullable = false, length = 64)
    private String receiverId;

    /**
     * 发起人账号
     */
    @Column(length = 64)
    private String initiatorId;

    /**
     * 待办标题
     */
    @Column(nullable = false, length = 512)
    private String title;

    /**
     * 待办详情内容
     */
    @Column(columnDefinition = "text")
    private String content;

    /**
     * 优先级：1普通 2重要 3紧急
     */
    @Column(nullable = false)
    private Integer priority = 1;

    /**
     * 状态：1待处理 2处理中 3已完成 4已撤销 5已过期
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 待办创建时间
     */
    @Column(nullable = false)
    private Date createTime;

    /**
     * 处理截止时间
     */
    private Date dueDate;

    /**
     * 完成时间
     */
    private Date completeTime;

    /**
     * 单据详情跳转地址
     */
    @Column(nullable = false, length = 1024)
    private String formUrl;

    /**
     * 关联Camunda任务ID
     */
    @Column(length = 64)
    private String camundaTaskId;

    /**
     * 关联Camunda流程实例ID
     */
    @Column(length = 64)
    private String camundaProcessInstanceId;

    /**
     * 是否已触发SLA告警
     */
    @Column(nullable = false)
    private Integer isSlaWarn = 0;

    /**
     * SLA升级级别
     */
    @Column(nullable = false)
    private Integer slaEscalateLevel = 0;

    /**
     * 备注
     */
    @Column(length = 1024)
    private String remark;

    @Column(length = 64)
    private String createBy;

    @Column(length = 64)
    private String updateBy;

    @Column(nullable = false)
    private Date updateTime = new Date();

    @Column(nullable = false)
    private Integer deleted = 0;
}
