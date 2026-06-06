package com.prison.controller;

import com.prison.dto.TemporaryLimitDTO;
import com.prison.entity.TemporaryLimitAdjustment;
import com.prison.service.TemporaryLimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/temporary-limits")
@RequiredArgsConstructor
public class TemporaryLimitController {

    private final TemporaryLimitService temporaryLimitService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TemporaryLimitDTO dto) {
        try {
            TemporaryLimitAdjustment adjustment = temporaryLimitService.createTemporaryLimit(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(adjustment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason) {
        try {
            TemporaryLimitAdjustment adjustment = temporaryLimitService.approveTemporaryLimit(
                    id, approverId, approved, rejectReason);
            return ResponseEntity.ok(adjustment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(temporaryLimitService.getTemporaryLimitById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long inmateId,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TemporaryLimitAdjustment> result = temporaryLimitService.listTemporaryLimits(
                inmateId, status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inmate/{inmateId}/active")
    public ResponseEntity<?> getActiveAdjustments(@PathVariable Long inmateId) {
        List<TemporaryLimitAdjustment> adjustments = temporaryLimitService.getActiveAdjustments(inmateId);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/stats/pending-count")
    public ResponseEntity<?> getPendingCount() {
        long count = temporaryLimitService.countPendingApproval();
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }
}
