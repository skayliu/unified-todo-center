package com.todo.center.repository;

import com.todo.center.entity.UniTodoOperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UniTodoOperLogRepository extends JpaRepository<UniTodoOperLog, Long>, JpaSpecificationExecutor<UniTodoOperLog> {
}
