package com.prison.repository;

import com.prison.entity.ConsumptionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionOrderItemRepository extends JpaRepository<ConsumptionOrderItem, Long> {

    List<ConsumptionOrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQty, SUM(oi.subtotal) as totalAmount " +
           "FROM ConsumptionOrderItem oi JOIN oi.order o " +
           "WHERE o.status = '已完成' AND o.createdAt >= :startTime AND o.createdAt < :endTime " +
           "GROUP BY oi.productId, oi.productName ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT p.category, SUM(oi.quantity) as totalQty, SUM(oi.subtotal) as totalAmount " +
           "FROM ConsumptionOrderItem oi JOIN oi.order o JOIN Product p ON oi.productId = p.id " +
           "WHERE o.status = '已完成' AND o.createdAt >= :startTime AND o.createdAt < :endTime " +
           "GROUP BY p.category")
    List<Object[]> getSalesByCategory(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM ConsumptionOrderItem oi " +
           "JOIN oi.order o WHERE o.inmateId = :inmateId AND o.status = '已完成' " +
           "AND oi.productId = :productId " +
           "AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    Integer getMonthlyPurchasedQuantity(@Param("inmateId") Long inmateId,
                                        @Param("productId") Long productId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
}
