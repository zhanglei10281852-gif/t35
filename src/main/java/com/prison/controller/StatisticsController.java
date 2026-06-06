package com.prison.controller;

import com.prison.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly-sales")
    public ResponseEntity<?> getMonthlySalesSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        Map<String, Object> result = statisticsService.getMonthlySalesSummary(y, m);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category-sales-ratio")
    public ResponseEntity<?> getCategorySalesRatio(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        List<Map<String, Object>> result = statisticsService.getCategorySalesRatio(y, m);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/average-consumption")
    public ResponseEntity<?> getAverageMonthlyConsumption(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        Map<String, Object> result = statisticsService.getAverageMonthlyConsumption(y, m);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopSellingProducts(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        List<Map<String, Object>> result = statisticsService.getTopSellingProducts(y, m, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/balance-distribution")
    public ResponseEntity<?> getAccountBalanceDistribution() {
        List<Map<String, Object>> result = statisticsService.getAccountBalanceDistribution();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/remittance-trend")
    public ResponseEntity<?> getRemittanceTrend(
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer startMonth,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Integer endMonth) {
        LocalDate now = LocalDate.now();
        int sy = startYear != null ? startYear : now.minusMonths(5).getYear();
        int sm = startMonth != null ? startMonth : now.minusMonths(5).getMonthValue();
        int ey = endYear != null ? endYear : now.getYear();
        int em = endMonth != null ? endMonth : now.getMonthValue();
        List<Map<String, Object>> result = statisticsService.getMonthlyRemittanceTrend(sy, sm, ey, em);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<?> getSalesTrend(
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer startMonth,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Integer endMonth) {
        LocalDate now = LocalDate.now();
        int sy = startYear != null ? startYear : now.minusMonths(5).getYear();
        int sm = startMonth != null ? startMonth : now.minusMonths(5).getMonthValue();
        int ey = endYear != null ? endYear : now.getYear();
        int em = endMonth != null ? endMonth : now.getMonthValue();
        Map<String, Object> result = statisticsService.getMonthlySalesTrend(sy, sm, ey, em);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> result = statisticsService.getDashboardStats();
        return ResponseEntity.ok(result);
    }
}
