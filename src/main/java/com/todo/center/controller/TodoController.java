package com.todo.center.controller;

import com.todo.center.entity.UniTodoMain;
import com.todo.center.service.TodoCoreService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一待办对外接口控制器
 */
@RestController
@RequestMapping("/v1/todo")
public class TodoController {

    @Resource
    private TodoCoreService todoCoreService;

    /**
     * 查询我的待办列表
     */
    @GetMapping("/mine")
    public Map<String, Object> myTodoList(@RequestParam String userId) {
        List<UniTodoMain> list = todoCoreService.queryUserPendingTodo(userId);
        return success(list);
    }

    /**
     * 外部系统同步待办接口
     */
    @PostMapping("/sync")
    public Map<String, Object> syncTodo(@RequestBody UniTodoMain todo,
                                        @RequestHeader(value = "X-User-Id", defaultValue = "system") String operator) {
        String result = todoCoreService.syncOuterTodo(todo, operator);
        return success(result);
    }

    /**
     * 处理完成单个待办
     */
    @PostMapping("/complete")
    public Map<String, Object> complete(@RequestBody Map<String, Object> params,
                                        HttpServletRequest request,
                                        @RequestHeader("X-User-Id") String userId) {
        String todoId = (String) params.get("todoId");
        String comment = (String) params.getOrDefault("comment", "");
        HashMap<String, Object> variables = (HashMap<String, Object>) params.getOrDefault("variables", new HashMap<>());
        String ip = getClientIp(request);
        todoCoreService.completeTodo(todoId, userId, variables, comment, ip);
        return success("ok");
    }

    /**
     * 批量审批
     */
    @PostMapping("/batch-complete")
    public Map<String, Object> batchComplete(@RequestBody Map<String, Object> params,
                                             HttpServletRequest request,
                                             @RequestHeader("X-User-Id") String userId) {
        List<String> todoIds = (List<String>) params.get("todoIds");
        String comment = (String) params.getOrDefault("comment", "批量审批");
        String ip = getClientIp(request);
        Map<String, Object> result = todoCoreService.batchComplete(todoIds, userId, comment, ip);
        return success(result);
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
