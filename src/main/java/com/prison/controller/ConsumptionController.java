package com.prison.controller;

import com.prison.dto.ConsumptionRequestDTO;
import com.prison.entity.ConsumptionOrder;
import com.prison.entity.ConsumptionOrderItem;
import com.prison.service.ConsumptionService;
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
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody ConsumptionRequestDTO dto) {
        try {
            Map<String, Object> result = consumptionService.createConsumptionOrder(dto);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<?> processOrder(@PathVariable Long id) {
        try {
            ConsumptionOrder order = consumptionService.processOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveOrder(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason) {
        try {
            ConsumptionOrder order = consumptionService.approveOrder(id, approverId, approved, rejectReason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(consumptionService.getOrderById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/orderNo/{orderNo}")
    public ResponseEntity<?> getByOrderNo(@PathVariable String orderNo) {
        try {
            return ResponseEntity.ok(consumptionService.getOrderByNo(orderNo));
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
        Page<ConsumptionOrder> result = consumptionService.listOrders(inmateId, status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        List<ConsumptionOrderItem> items = consumptionService.getOrderItems(orderId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/inmate/{inmateId}/monthly-info")
    public ResponseEntity<?> getMonthlyConsumptionInfo(@PathVariable Long inmateId) {
        try {
            Map<String, Object> info = consumptionService.getMonthlyConsumptionInfo(inmateId);
            return ResponseEntity.ok(info);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
