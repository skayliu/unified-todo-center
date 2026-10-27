package com.todo.center;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 统一待办中心启动类
 * 基于Camunda 7.24 + Spring Boot 3.5.5 + JDK17
 *
 * @author unified-todo-center
 */
@SpringBootApplication
@EnableProcessApplication("unified-todo-center")
@EnableAsync
@EnableScheduling
public class UnifiedTodoCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnifiedTodoCenterApplication.class, args);
        System.out.println("""
                ================================================
                统一待办中心启动成功!
                - Camunda控制台: http://localhost:8080/todo-api/camunda
                - 默认账号: admin / admin123
                - API前缀: /todo-api
                ================================================
                """);
    }
}
