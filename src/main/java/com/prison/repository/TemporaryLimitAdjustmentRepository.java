package com.prison.repository;

import com.prison.entity.TemporaryLimitAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TemporaryLimitAdjustmentRepository extends JpaRepository<TemporaryLimitAdjustment, Long> {

    Page<TemporaryLimitAdjustment> findByInmateId(Long inmateId, Pageable pageable);

    Page<TemporaryLimitAdjustment> findByStatus(String status, Pageable pageable);

    @Query("SELECT t FROM TemporaryLimitAdjustment t " +
           "WHERE t.inmateId = :inmateId AND t.status = '已通过' " +
           "AND t.effectiveDate <= :date AND t.expiryDate >= :date")
    List<TemporaryLimitAdjustment> findActiveAdjustments(@Param("inmateId") Long inmateId,
                                                          @Param("date") LocalDate date);

    Optional<TemporaryLimitAdjustment> findFirstByInmateIdAndStatusOrderByCreatedAtDesc(Long inmateId, String status);

    long countByStatus(String status);
}
