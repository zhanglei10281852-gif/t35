package com.prison.repository;

import com.prison.entity.InmateAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InmateAccountRepository extends JpaRepository<InmateAccount, Long> {

    Optional<InmateAccount> findByInmateId(Long inmateId);

    Optional<InmateAccount> findByAccountNo(String accountNo);

    Page<InmateAccount> findByStatus(String status, Pageable pageable);

    @Query("SELECT a FROM InmateAccount a WHERE a.balance > 0")
    List<InmateAccount> findActiveAccounts();

    @Query("SELECT " +
           "CASE WHEN a.balance = 0 THEN '零余额' " +
           "WHEN a.balance < 100 THEN '0-100' " +
           "WHEN a.balance < 500 THEN '100-500' " +
           "WHEN a.balance < 1000 THEN '500-1000' " +
           "WHEN a.balance < 2000 THEN '1000-2000' " +
           "ELSE '2000+' END as range, " +
           "COUNT(a) as count " +
           "FROM InmateAccount a GROUP BY " +
           "CASE WHEN a.balance = 0 THEN '零余额' " +
           "WHEN a.balance < 100 THEN '0-100' " +
           "WHEN a.balance < 500 THEN '100-500' " +
           "WHEN a.balance < 1000 THEN '500-1000' " +
           "WHEN a.balance < 2000 THEN '1000-2000' " +
           "ELSE '2000+' END")
    List<Object[]> getBalanceDistribution();
}
