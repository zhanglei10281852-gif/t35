package com.prison.repository;

import com.prison.entity.ReturnRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {

    Optional<ReturnRecord> findByReturnNo(String returnNo);

    Page<ReturnRecord> findByInmateId(Long inmateId, Pageable pageable);

    Page<ReturnRecord> findByStatus(String status, Pageable pageable);

    Page<ReturnRecord> findByOrderId(Long orderId, Pageable pageable);

    long countByStatus(String status);
}
