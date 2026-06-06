package com.prison.controller;

import com.prison.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly-sales")
    public ResponseEntity<?> getMonthlySalesSummary(
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> result = statisticsService.getMonthlySalesSummary(year, month);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category-sales-ratio")
    public ResponseEntity<?> getCategorySalesRatio(
            @RequestParam int year,
            @RequestParam int month) {
        List<Map<String, Object>> result = statisticsService.getCategorySalesRatio(year, month);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/average-consumption")
    public ResponseEntity<?> getAverageMonthlyConsumption(
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> result = statisticsService.getAverageMonthlyConsumption(year, month);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopSellingProducts(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> result = statisticsService.getTopSellingProducts(year, month, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/balance-distribution")
    public ResponseEntity<?> getAccountBalanceDistribution() {
        List<Map<String, Object>> result = statisticsService.getAccountBalanceDistribution();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/remittance-trend")
    public ResponseEntity<?> getRemittanceTrend(
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth) {
        List<Map<String, Object>> result = statisticsService.getMonthlyRemittanceTrend(
                startYear, startMonth, endYear, endMonth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<?> getSalesTrend(
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth) {
        Map<String, Object> result = statisticsService.getMonthlySalesTrend(
                startYear, startMonth, endYear, endMonth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> result = statisticsService.getDashboardStats();
        return ResponseEntity.ok(result);
    }
}
