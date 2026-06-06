package com.prison.service;

import com.prison.dto.ConsumptionItemDTO;
import com.prison.dto.ConsumptionRequestDTO;
import com.prison.entity.ConsumptionOrder;
import com.prison.entity.ConsumptionOrderItem;
import com.prison.entity.InmateAccount;
import com.prison.entity.Product;
import com.prison.repository.ConsumptionOrderItemRepository;
import com.prison.repository.ConsumptionOrderRepository;
import com.prison.repository.InmateAccountRepository;
import com.prison.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final ConsumptionOrderRepository consumptionOrderRepository;
    private final ConsumptionOrderItemRepository consumptionOrderItemRepository;
    private final ProductRepository productRepository;
    private final InmateAccountRepository inmateAccountRepository;
    private final AccountService accountService;

    @Transactional
    public Map<String, Object> createConsumptionOrder(ConsumptionRequestDTO requestDTO) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        boolean needsApproval = false;
        List<ConsumptionOrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        InmateAccount account = inmateAccountRepository.findByInmateId(requestDTO.getInmateId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        if (!"正常".equals(account.getStatus())) {
            throw new RuntimeException("账户状态异常，无法消费");
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal monthlyConsumed = consumptionOrderRepository.getMonthlyConsumedAmount(
                requestDTO.getInmateId(), monthStart, monthEnd);

        BigDecimal effectiveLimit = accountService.getEffectiveMonthlyLimit(requestDTO.getInmateId());

        for (ConsumptionItemDTO itemDTO : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + itemDTO.getProductId()));

            if (!product.getIsOnSale()) {
                errors.add("商品已下架: " + product.getName());
                continue;
            }

            if ("禁止购买".equals(product.getPurchaseRestrictionLevel())) {
                errors.add("商品禁止购买: " + product.getName());
                continue;
            }

            if ("需审批".equals(product.getPurchaseRestrictionLevel())) {
                needsApproval = true;
            }

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                errors.add("库存不足: " + product.getName() + "，库存: " + product.getStockQuantity());
                continue;
            }

            if (product.getMonthlyLimitPerPerson() != null && product.getMonthlyLimitPerPerson() > 0) {
                Integer monthlyPurchased = consumptionOrderItemRepository.getMonthlyPurchasedQuantity(
                        requestDTO.getInmateId(), product.getId(), monthStart, monthEnd);
                int totalPurchased = (monthlyPurchased == null ? 0 : monthlyPurchased) + itemDTO.getQuantity();
                if (totalPurchased > product.getMonthlyLimitPerPerson()) {
                    errors.add("超出月限购数量: " + product.getName() +
                            "，限购: " + product.getMonthlyLimitPerPerson() +
                            "，已购: " + (monthlyPurchased == null ? 0 : monthlyPurchased));
                    continue;
                }
            }

            BigDecimal subtotal = product.getUnitPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            ConsumptionOrderItem orderItem = new ConsumptionOrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getUnitPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setReturnedQuantity(0);
            orderItems.add(orderItem);
        }

        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("errors", errors);
            return result;
        }

        BigDecimal newMonthlyTotal = monthlyConsumed.add(totalAmount);
        if (newMonthlyTotal.compareTo(effectiveLimit) > 0) {
            result.put("success", false);
            result.put("errors", List.of("超出月消费限额，本月已消费: " + monthlyConsumed +
                    "，剩余额度: " + effectiveLimit.subtract(monthlyConsumed) +
                    "，本次消费: " + totalAmount));
            return result;
        }

        if (account.getBalance().compareTo(totalAmount) < 0) {
            result.put("success", false);
            result.put("errors", List.of("账户余额不足，余额: " + account.getBalance()));
            return result;
        }

        String orderNo = generateOrderNo();
        ConsumptionOrder order = new ConsumptionOrder();
        order.setOrderNo(orderNo);
        order.setInmateId(requestDTO.getInmateId());
        order.setTotalAmount(totalAmount);
        order.setStatus(needsApproval ? "待审批" : "待扣款");
        ConsumptionOrder savedOrder = consumptionOrderRepository.save(order);

        for (ConsumptionOrderItem item : orderItems) {
            item.setOrder(savedOrder);
            consumptionOrderItemRepository.save(item);
        }
        savedOrder.setItems(orderItems);

        if (!needsApproval) {
            processOrder(savedOrder.getId());
            savedOrder = consumptionOrderRepository.findById(savedOrder.getId()).orElse(savedOrder);
        }

        result.put("success", true);
        result.put("needsApproval", needsApproval);
        result.put("order", savedOrder);
        return result;
    }

    @Transactional
    public ConsumptionOrder processOrder(Long orderId) {
        ConsumptionOrder order = consumptionOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"待扣款".equals(order.getStatus()) && !"审批通过".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许扣款");
        }

        InmateAccount account = inmateAccountRepository.findByInmateId(order.getInmateId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        if (account.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new RuntimeException("账户余额不足");
        }

        List<ConsumptionOrderItem> items = consumptionOrderItemRepository.findByOrderId(order.getId());
        for (ConsumptionOrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        account.setBalance(account.getBalance().subtract(order.getTotalAmount()));
        inmateAccountRepository.save(account);

        order.setStatus("已完成");
        return consumptionOrderRepository.save(order);
    }

    @Transactional
    public ConsumptionOrder approveOrder(Long orderId, Long approverId, boolean approved, String rejectReason) {
        ConsumptionOrder order = consumptionOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"待审批".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许审批");
        }

        order.setApprovedBy(approverId);
        order.setApprovedAt(LocalDateTime.now());

        if (approved) {
            order.setStatus("审批通过");
            processOrder(orderId);
        } else {
            order.setStatus("已拒绝");
            order.setRejectReason(rejectReason);
        }

        return consumptionOrderRepository.save(order);
    }

    public ConsumptionOrder getOrderById(Long id) {
        return consumptionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public ConsumptionOrder getOrderByNo(String orderNo) {
        return consumptionOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public Page<ConsumptionOrder> listOrders(Long inmateId, String status, Pageable pageable) {
        if (inmateId != null && status != null && !status.isBlank()) {
            return consumptionOrderRepository.findByInmateIdAndStatus(inmateId, status, pageable);
        }
        if (inmateId != null) {
            return consumptionOrderRepository.findByInmateId(inmateId, pageable);
        }
        if (status != null && !status.isBlank()) {
            return consumptionOrderRepository.findByStatus(status, pageable);
        }
        return consumptionOrderRepository.findAll(pageable);
    }

    public List<ConsumptionOrderItem> getOrderItems(Long orderId) {
        return consumptionOrderItemRepository.findByOrderId(orderId);
    }

    public Map<String, Object> getMonthlyConsumptionInfo(Long inmateId) {
        Map<String, Object> result = new HashMap<>();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal consumed = consumptionOrderRepository.getMonthlyConsumedAmount(inmateId, monthStart, monthEnd);
        BigDecimal effectiveLimit = accountService.getEffectiveMonthlyLimit(inmateId);
        BigDecimal remaining = effectiveLimit.subtract(consumed);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        result.put("inmateId", inmateId);
        result.put("month", currentMonth.toString());
        result.put("consumedAmount", consumed);
        result.put("effectiveLimit", effectiveLimit);
        result.put("remainingLimit", remaining);
        return result;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
