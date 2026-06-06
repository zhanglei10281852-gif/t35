package com.prison.controller;

import com.prison.dto.InventoryCheckDTO;
import com.prison.entity.InventoryCheck;
import com.prison.entity.InventoryCheckItem;
import com.prison.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/check")
    public ResponseEntity<?> createCheck(@Valid @RequestBody InventoryCheckDTO dto) {
        try {
            InventoryCheck check = inventoryService.createInventoryCheck(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(check);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/checks/{id}")
    public ResponseEntity<?> getCheckById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventoryService.getInventoryCheckById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/checks/no/{checkNo}")
    public ResponseEntity<?> getCheckByNo(@PathVariable String checkNo) {
        try {
            return ResponseEntity.ok(inventoryService.getInventoryCheckByNo(checkNo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/checks")
    public ResponseEntity<?> listChecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("checkDate").descending());
        Page<InventoryCheck> result = inventoryService.listInventoryChecks(startDate, endDate, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/checks/{checkId}/items")
    public ResponseEntity<?> getCheckItems(@PathVariable Long checkId) {
        List<InventoryCheckItem> items = inventoryService.getCheckItems(checkId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/checks/{checkId}/adjust")
    public ResponseEntity<?> adjustStock(@PathVariable Long checkId) {
        try {
            inventoryService.adjustStockByCheck(checkId);
            return ResponseEntity.ok(Map.of("message", "库存调整成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/checks/monthly/{month}")
    public ResponseEntity<?> getMonthlyChecks(@PathVariable String month) {
        List<InventoryCheck> checks = inventoryService.getMonthlyChecks(month);
        return ResponseEntity.ok(checks);
    }
}
