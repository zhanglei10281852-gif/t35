package com.prison.repository;

import com.prison.entity.RemittanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RemittanceRecordRepository extends JpaRepository<RemittanceRecord, Long> {

    Page<RemittanceRecord> findByInmateId(Long inmateId, Pageable pageable);

    Page<RemittanceRecord> findByStatus(String status, Pageable pageable);

    List<RemittanceRecord> findByInmateIdAndStatus(Long inmateId, String status);

    @Query("SELECT SUM(r.amount) FROM RemittanceRecord r WHERE r.inmateId = :inmateId AND r.status = '已到账'")
    BigDecimal sumByInmateId(@Param("inmateId") Long inmateId);

    @Query("SELECT FUNCTION('DATE_FORMAT', r.remittanceDate, '%Y-%m') as month, SUM(r.amount) as total " +
           "FROM RemittanceRecord r WHERE r.remittanceDate >= :startDate AND r.remittanceDate <= :endDate " +
           "AND r.status = '已到账' GROUP BY FUNCTION('DATE_FORMAT', r.remittanceDate, '%Y-%m') " +
           "ORDER BY month")
    List<Object[]> getMonthlyRemittanceTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
