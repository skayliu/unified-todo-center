# 企业级统一待办中心 - Spring Boot 工程

## 技术栈版本
| 组件 | 版本 |
|------|------|
| JDK | 17 |
| Spring Boot | 3.5.5 |
| Camunda BPM | 7.24.0 |
| Flyway | Spring Boot 内置版本 |
| 数据库 | MySQL 8.0+ |
| ORM | Spring Data JPA |

## 快速开始
### 1. 环境准备
- JDK 17+
- MySQL 8.0+，创建数据库 `unified_todo`
- Maven 3.8+

### 2. 修改配置
编辑 `src/main/resources/application.yml`，修改数据库连接地址、用户名密码

### 3. 启动项目
```bash
mvn clean spring-boot:run
```

### 4. 访问地址
- Camunda 控制台: http://localhost:8080/todo-api/camunda
- 默认管理员账号: admin / admin123
- 待办接口前缀: http://localhost:8080/todo-api/v1/todo

## 核心能力
✅ 零侵入集成Camunda 7.24引擎，通过事件监听器自动同步任务

✅ 四元组幂等校验机制，彻底杜绝重复待办

✅ Flyway统一管理数据库版本，自动建表升级

✅ 多渠道异步消息通知，支持失败重试

✅ SLA超时自动扫描升级定时任务

✅ 完整审计操作日志

✅ 批量审批能力

✅ 外部系统Webhook/API待办同步

✅ 跨系统状态最终一致性保障

## 目录结构
```
src/main/java/com/todo/center
├── config/          # 配置类
├── controller/      # 对外REST接口
├── entity/          # JPA实体
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
├── job/             # 定时任务
└── listener/        # Camunda引擎事件监听器

src/main/resources
├── application.yml  # 配置文件
└── db/migration/    # Flyway数据库脚本
```

## 核心接口
| 接口 | 方法 | 说明 |
|------|------|------|
| /v1/todo/mine | GET | 查询我的待办列表 |
| /v1/todo/sync | POST | 外部系统同步待办 |
| /v1/todo/complete | POST | 完成单个待办 |
| /v1/todo/batch-complete | POST | 批量审批待办 |

## 数据库说明
- Camunda官方表由Flyway基线管理，不修改引擎原生表结构
- 扩展表统一以 `uni_todo_` 前缀命名，与引擎表完全隔离
- 所有业务表包含逻辑删除字段和审计字段
