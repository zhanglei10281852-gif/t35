package com.prison.service;

import com.prison.repository.ConsumptionOrderItemRepository;
import com.prison.repository.ConsumptionOrderRepository;
import com.prison.repository.InmateAccountRepository;
import com.prison.repository.RemittanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ConsumptionOrderRepository consumptionOrderRepository;
    private final ConsumptionOrderItemRepository consumptionOrderItemRepository;
    private final RemittanceRecordRepository remittanceRecordRepository;
    private final InmateAccountRepository inmateAccountRepository;

    public Map<String, Object> getMonthlySalesSummary(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

        Map<String, Object> result = new HashMap<>();
        BigDecimal totalSales = consumptionOrderRepository.getTotalSalesByDateRange(startTime, endTime);
        result.put("year", year);
        result.put("month", month);
        result.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        return result;
    }

    public List<Map<String, Object>> getCategorySalesRatio(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Object[]> rawData = consumptionOrderItemRepository.getSalesByCategory(startTime, endTime);
        List<Map<String, Object>> result = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Object[] row : rawData) {
            totalAmount = totalAmount.add((BigDecimal) row[2]);
        }

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            String category = (String) row[0];
            Long totalQty = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];
            BigDecimal ratio = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                ratio = amount.multiply(new BigDecimal("100")).divide(totalAmount, 2, java.math.RoundingMode.HALF_UP);
            }
            item.put("category", category);
            item.put("totalQuantity", totalQty);
            item.put("totalAmount", amount);
            item.put("ratio", ratio);
            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getAverageMonthlyConsumption(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalSales = consumptionOrderRepository.getTotalSalesByDateRange(startTime, endTime);
        long accountCount = inmateAccountRepository.count();

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        result.put("accountCount", accountCount);
        BigDecimal average = BigDecimal.ZERO;
        if (accountCount > 0 && totalSales != null) {
            average = totalSales.divide(new BigDecimal(accountCount), 2, java.math.RoundingMode.HALF_UP);
        }
        result.put("averageConsumption", average);
        return result;
    }

    public List<Map<String, Object>> getTopSellingProducts(int year, int month, int limit) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Object[]> rawData = consumptionOrderItemRepository.getTopSellingProducts(startTime, endTime);
        List<Map<String, Object>> result = new ArrayList<>();

        int count = 0;
        for (Object[] row : rawData) {
            if (count >= limit) break;
            Map<String, Object> item = new HashMap<>();
            item.put("productId", row[0]);
            item.put("productName", row[1]);
            item.put("totalQuantity", ((Number) row[2]).longValue());
            item.put("totalAmount", row[3]);
            item.put("rank", count + 1);
            result.add(item);
            count++;
        }

        return result;
    }

    public List<Map<String, Object>> getAccountBalanceDistribution() {
        List<Object[]> rawData = inmateAccountRepository.getBalanceDistribution();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("range", row[0]);
            item.put("count", ((Number) row[1]).longValue());
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getMonthlyRemittanceTrend(int startYear, int startMonth, int endYear, int endMonth) {
        LocalDate startDate = LocalDate.of(startYear, startMonth, 1);
        LocalDate endDate = LocalDate.of(endYear, endMonth, YearMonth.of(endYear, endMonth).lengthOfMonth());

        List<Object[]> rawData = remittanceRecordRepository.getMonthlyRemittanceTrend(startDate, endDate);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", row[0]);
            item.put("totalAmount", row[1]);
            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getMonthlySalesTrend(int startYear, int startMonth, int endYear, int endMonth) {
        LocalDateTime startTime = LocalDate.of(startYear, startMonth, 1).atStartOfDay();
        LocalDateTime endTime = LocalDate.of(endYear, endMonth, YearMonth.of(endYear, endMonth).lengthOfMonth()).atTime(23, 59, 59);

        List<Object[]> rawData = consumptionOrderRepository.getMonthlySales(startTime, endTime);
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", row[0]);
            BigDecimal amount = (BigDecimal) row[1];
            item.put("totalSales", amount);
            total = total.add(amount);
            monthlyData.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyData", monthlyData);
        result.put("totalPeriodSales", total);
        return result;
    }

    public Map<String, Object> getDashboardStats() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        Map<String, Object> result = new HashMap<>();

        BigDecimal monthlySales = consumptionOrderRepository.getTotalSalesByDateRange(monthStart, monthEnd);
        result.put("monthlySales", monthlySales != null ? monthlySales : BigDecimal.ZERO);

        long pendingOrders = consumptionOrderRepository.countPendingApproval();
        result.put("pendingOrders", pendingOrders);

        long totalAccounts = inmateAccountRepository.count();
        result.put("totalAccounts", totalAccounts);

        List<Map<String, Object>> topProducts = getTopSellingProducts(today.getYear(), today.getMonthValue(), 5);
        result.put("topSellingProducts", topProducts);

        return result;
    }
}
