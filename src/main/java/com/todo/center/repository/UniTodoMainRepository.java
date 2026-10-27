package com.todo.center.repository;

import com.todo.center.entity.UniTodoMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 统一待办数据访问层
 */
@Repository
public interface UniTodoMainRepository extends JpaRepository<UniTodoMain, Long>, JpaSpecificationExecutor<UniTodoMain> {

    /**
     * 四元组幂等查询
     */
    boolean existsByAppIdAndBizTypeAndBizIdAndReceiverId(
            String appId, String bizType, String bizId, String receiverId
    );

    Optional<UniTodoMain> findByTodoId(String todoId);

    Optional<UniTodoMain> findByCamundaTaskId(String camundaTaskId);

    List<UniTodoMain> findByReceiverIdAndStatusOrderByCreateTimeDesc(String receiverId, Integer status);

    List<UniTodoMain> findByCamundaTaskIdIn(List<String> camundaTaskIds);

    /**
     * 查询所有已到期未处理且未告警的待办
     */
    List<UniTodoMain> findByDueDateBeforeAndStatusAndIsSlaWarn(Date now, Integer status, Integer isSlaWarn);

    /**
     * 查询我的发起
     */
    List<UniTodoMain> findByInitiatorIdOrderByCreateTimeDesc(String initiatorId);
}
