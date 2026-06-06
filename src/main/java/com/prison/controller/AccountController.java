package com.prison.controller;

import com.prison.dto.RemittanceDTO;
import com.prison.entity.InmateAccount;
import com.prison.entity.RemittanceRecord;
import com.prison.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{inmateId}")
    public ResponseEntity<?> createAccount(@PathVariable Long inmateId) {
        try {
            InmateAccount account = accountService.createAccount(inmateId);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(accountService.getAccountById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmateId(@PathVariable Long inmateId) {
        try {
            return ResponseEntity.ok(accountService.getAccountByInmateId(inmateId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InmateAccount> result = accountService.listAccounts(status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{inmateId}/monthly-limit")
    public ResponseEntity<?> updateMonthlyLimit(@PathVariable Long inmateId, @RequestParam BigDecimal newLimit) {
        try {
            InmateAccount account = accountService.updateMonthlyLimit(inmateId, newLimit);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{inmateId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long inmateId, @RequestParam String status) {
        try {
            InmateAccount account = accountService.updateAccountStatus(inmateId, status);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/remittance")
    public ResponseEntity<?> addRemittance(@Valid @RequestBody RemittanceDTO dto) {
        try {
            RemittanceRecord record = accountService.addRemittance(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/remittances")
    public ResponseEntity<?> listRemittances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long inmateId,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RemittanceRecord> result = accountService.listRemittances(inmateId, status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{inmateId}/effective-limit")
    public ResponseEntity<?> getEffectiveMonthlyLimit(@PathVariable Long inmateId) {
        try {
            BigDecimal limit = accountService.getEffectiveMonthlyLimit(inmateId);
            return ResponseEntity.ok(Map.of("inmateId", inmateId, "effectiveLimit", limit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{inmateId}/limit-info")
    public ResponseEntity<?> getMonthlyLimitInfo(@PathVariable Long inmateId) {
        try {
            Map<String, Object> info = accountService.getMonthlyLimitInfo(inmateId);
            return ResponseEntity.ok(info);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats/balance-distribution")
    public ResponseEntity<?> getBalanceDistribution() {
        List<Object[]> distribution = accountService.getBalanceDistribution();
        return ResponseEntity.ok(distribution);
    }
}
