package com.prison.repository;

import com.prison.entity.InventoryCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {

    Optional<InventoryCheck> findByCheckNo(String checkNo);

    Page<InventoryCheck> findByCheckDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT ic FROM InventoryCheck ic WHERE FUNCTION('DATE_FORMAT', ic.checkDate, '%Y-%m') = :month ORDER BY ic.checkDate DESC")
    List<InventoryCheck> findByMonth(@Param("month") String month);
}
