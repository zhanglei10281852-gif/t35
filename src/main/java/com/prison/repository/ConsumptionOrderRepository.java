package com.prison.repository;

import com.prison.entity.ConsumptionOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsumptionOrderRepository extends JpaRepository<ConsumptionOrder, Long> {

    Optional<ConsumptionOrder> findByOrderNo(String orderNo);

    Page<ConsumptionOrder> findByInmateId(Long inmateId, Pageable pageable);

    Page<ConsumptionOrder> findByStatus(String status, Pageable pageable);

    Page<ConsumptionOrder> findByInmateIdAndStatus(Long inmateId, String status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM ConsumptionOrder o " +
           "WHERE o.inmateId = :inmateId AND o.status = '已完成' " +
           "AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    BigDecimal getMonthlyConsumedAmount(@Param("inmateId") Long inmateId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM ConsumptionOrder o " +
           "WHERE o.status = '已完成' AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    BigDecimal getTotalSalesByDateRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(o) FROM ConsumptionOrder o WHERE o.status = '待审批'")
    long countPendingApproval();

    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') as month, SUM(o.totalAmount) as total " +
           "FROM ConsumptionOrder o WHERE o.status = '已完成' " +
           "AND o.createdAt >= :startTime AND o.createdAt < :endTime " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') ORDER BY month")
    List<Object[]> getMonthlySales(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
}
