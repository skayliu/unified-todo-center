-- 统一待办中心业务表结构版本 2.0
-- 创建时间: 2026-10-27
-- 注意: Camunda自身表由引擎自动创建基线, 本脚本仅创建待办扩展表

-- 统一待办主表
CREATE TABLE IF NOT EXISTS `uni_todo_main` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `todo_id` varchar(64) NOT NULL COMMENT '待办全局唯一ID',
  `app_id` varchar(64) NOT NULL COMMENT '源系统标识',
  `biz_type` varchar(64) NOT NULL COMMENT '业务类型',
  `biz_id` varchar(64) NOT NULL COMMENT '源系统业务ID',
  `receiver_id` varchar(64) NOT NULL COMMENT '处理人账号',
  `title` varchar(512) NOT NULL COMMENT '待办标题',
  `content` text COMMENT '待办详情内容',
  `priority` tinyint NOT NULL DEFAULT '1' COMMENT '优先级 1普通 2重要 3紧急',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1待处理 2处理中 3已完成 4已撤销 5已过期',
  `create_time` datetime NOT NULL COMMENT '待办创建时间',
  `due_date` datetime DEFAULT NULL COMMENT '处理截止时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `form_url` varchar(1024) NOT NULL COMMENT '单据详情跳转地址',
  `camunda_task_id` varchar(64) DEFAULT NULL COMMENT '关联Camunda任务ID',
  `camunda_process_instance_id` varchar(64) DEFAULT NULL COMMENT '关联Camunda流程实例ID',
  `is_sla_warn` tinyint NOT NULL DEFAULT '0' COMMENT '是否已触发SLA告警 0否 1是',
  `sla_escalate_level` tinyint NOT NULL DEFAULT '0' COMMENT 'SLA升级级别',
  `initiator_id` varchar(64) DEFAULT NULL COMMENT '发起人账号',
  `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_unique` (`app_id`,`biz_type`,`biz_id`,`receiver_id`),
  KEY `idx_todo_id` (`todo_id`),
  KEY `idx_receiver_status` (`receiver_id`,`status`),
  KEY `idx_camunda_task` (`camunda_task_id`),
  KEY `idx_process_instance` (`camunda_process_instance_id`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一待办主表';

-- 源系统接入配置表
CREATE TABLE IF NOT EXISTS `uni_todo_app_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` varchar(64) NOT NULL COMMENT '系统唯一标识',
  `app_name` varchar(128) NOT NULL COMMENT '系统名称',
  `app_icon` varchar(256) DEFAULT NULL COMMENT '系统图标URL',
  `callback_url` varchar(512) NOT NULL COMMENT '状态回写地址',
  `app_secret` varchar(128) NOT NULL COMMENT '接口签名密钥',
  `sync_type` varchar(32) NOT NULL DEFAULT 'webhook' COMMENT '接入方式 webhook/api/mq',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用 0禁用 1启用',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `description` varchar(512) DEFAULT NULL COMMENT '系统描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办接入系统配置表';

-- SLA规则配置表
CREATE TABLE IF NOT EXISTS `uni_todo_sla_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` varchar(64) NOT NULL COMMENT '关联系统ID',
  `biz_type` varchar(64) NOT NULL COMMENT '关联业务类型',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `warn_threshold_minute` int NOT NULL COMMENT '超时预警时长(分钟)',
  `escalate_threshold_minute` int NOT NULL COMMENT '升级上级时长(分钟)',
  `escalate_user_id` varchar(64) DEFAULT NULL COMMENT '升级目标处理人',
  `escalate_role_code` varchar(64) DEFAULT NULL COMMENT '升级角色编码',
  `notify_channel` varchar(128) NOT NULL COMMENT '通知渠道,多个逗号分隔',
  `is_enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_biz` (`app_id`,`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办SLA规则配置表';

-- 待办操作日志表
CREATE TABLE IF NOT EXISTS `uni_todo_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `todo_id` varchar(64) NOT NULL COMMENT '关联待办ID',
  `oper_type` varchar(32) NOT NULL COMMENT '操作类型 create/complete/transfer/cancel/escalate',
  `oper_user_id` varchar(64) NOT NULL COMMENT '操作人账号',
  `oper_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `oper_content` text COMMENT '操作详情备注',
  `from_user_id` varchar(64) DEFAULT NULL COMMENT '原处理人(转派场景)',
  `to_user_id` varchar(64) DEFAULT NULL COMMENT '目标处理人(转派/升级场景)',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '客户端UA',
  PRIMARY KEY (`id`),
  KEY `idx_todo_id` (`todo_id`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办操作审计日志表';

-- 待办通知推送记录表
CREATE TABLE IF NOT EXISTS `uni_todo_notify_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `todo_id` varchar(64) NOT NULL COMMENT '关联待办ID',
  `channel` varchar(32) NOT NULL COMMENT '通知渠道 feishu/dingtalk/wechat/email/sms',
  `receiver_id` varchar(64) NOT NULL COMMENT '接收人',
  `template_code` varchar(64) DEFAULT NULL COMMENT '消息模板编码',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `send_status` tinyint NOT NULL COMMENT '发送状态 0待发送 1发送成功 2发送失败',
  `fail_reason` varchar(1024) DEFAULT NULL COMMENT '失败原因',
  `retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `message_id` varchar(128) DEFAULT NULL COMMENT '第三方消息ID',
  `cost_time` int DEFAULT NULL COMMENT '推送耗时(毫秒)',
  PRIMARY KEY (`id`),
  KEY `idx_todo_channel` (`todo_id`,`channel`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办通知推送记录表';

-- 待办抄送关系表
CREATE TABLE IF NOT EXISTS `uni_todo_cc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `todo_id` varchar(64) NOT NULL COMMENT '待办ID',
  `cc_user_id` varchar(64) NOT NULL COMMENT '抄送人账号',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读 0未读 1已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_todo_cc_user` (`todo_id`,`cc_user_id`),
  KEY `idx_cc_user_read` (`cc_user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办抄送关系表';

-- 插入默认系统配置
INSERT INTO `uni_todo_app_config` (`app_id`, `app_name`, `callback_url`, `app_secret`, `sync_type`, `sort`, `description`) VALUES
('camunda-bpm', 'Camunda流程引擎', '', 'default-secret', 'event', 1, '内部Camunda工作流引擎原生待办'),
('oa-system', 'OA办公系统', 'http://oa.example.com/api/todo/callback', 'oa-secret-2026', 'webhook', 2, '企业内部OA办公流程'),
('erp-system', 'ERP采购系统', 'http://erp.example.com/api/todo/callback', 'erp-secret-2026', 'api', 3, 'ERP采购、入库审批流程'),
('finance-system', '财务费控系统', 'http://finance.example.com/api/todo/callback', 'finance-secret-2026', 'webhook', 4, '报销、付款、预算审批流程'),
('hr-system', 'HR人事系统', 'http://hr.example.com/api/todo/callback', 'hr-secret-2026', 'api', 5, '入职、转正、请假审批流程');

-- 插入默认SLA规则示例
INSERT INTO `uni_todo_sla_config` (`app_id`, `biz_type`, `rule_name`, `warn_threshold_minute`, `escalate_threshold_minute`, `notify_channel`, `description`) VALUES
('erp-system', 'purchase_order', '普通采购审批SLA', 120, 240, 'wechat,dingtalk', '普通采购单2小时预警,4小时升级'),
('finance-system', 'payment_apply', '大额付款审批SLA', 60, 120, 'wechat,dingtalk,sms', '付款审批1小时预警,2小时升级'),
('hr-system', 'leave_apply', '请假审批SLA', 240, 480, 'wechat', '请假审批4小时预警,8小时升级');
