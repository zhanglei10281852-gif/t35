package com.prison.controller;

import com.prison.dto.ReturnRequestDTO;
import com.prison.entity.ReturnItem;
import com.prison.entity.ReturnRecord;
import com.prison.service.ReturnService;
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
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    public ResponseEntity<?> createReturn(@Valid @RequestBody ReturnRequestDTO dto) {
        try {
            Map<String, Object> result = returnService.createReturnRequest(dto);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReturn(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason) {
        try {
            ReturnRecord record = returnService.approveReturn(id, approverId, approved, rejectReason);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(returnService.getReturnById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/returnNo/{returnNo}")
    public ResponseEntity<?> getByReturnNo(@PathVariable String returnNo) {
        try {
            return ResponseEntity.ok(returnService.getReturnByNo(returnNo));
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
        Page<ReturnRecord> result = returnService.listReturns(inmateId, status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{returnId}/items")
    public ResponseEntity<?> getReturnItems(@PathVariable Long returnId) {
        List<ReturnItem> items = returnService.getReturnItems(returnId);
        return ResponseEntity.ok(items);
    }
}
