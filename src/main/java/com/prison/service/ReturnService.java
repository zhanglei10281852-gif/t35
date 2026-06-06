package com.prison.service;

import com.prison.dto.ReturnItemDTO;
import com.prison.dto.ReturnRequestDTO;
import com.prison.entity.*;
import com.prison.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRecordRepository returnRecordRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ConsumptionOrderRepository consumptionOrderRepository;
    private final ConsumptionOrderItemRepository consumptionOrderItemRepository;
    private final ProductRepository productRepository;
    private final InmateAccountRepository inmateAccountRepository;

    @Transactional
    public Map<String, Object> createReturnRequest(ReturnRequestDTO requestDTO) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        ConsumptionOrder order = consumptionOrderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"已完成".equals(order.getStatus())) {
            result.put("success", false);
            result.put("errors", List.of("只有已完成的订单才能申请退货"));
            return result;
        }

        long daysBetween = ChronoUnit.DAYS.between(order.getCreatedAt(), LocalDateTime.now());
        if (daysBetween > 7) {
            result.put("success", false);
            result.put("errors", List.of("超出7天退货期限"));
            return result;
        }

        List<ReturnItem> returnItems = new ArrayList<>();
        BigDecimal totalReturnAmount = BigDecimal.ZERO;

        List<ConsumptionOrderItem> orderItems = consumptionOrderItemRepository.findByOrderId(order.getId());
        Map<Long, ConsumptionOrderItem> orderItemMap = new HashMap<>();
        for (ConsumptionOrderItem oi : orderItems) {
            orderItemMap.put(oi.getId(), oi);
        }

        for (ReturnItemDTO itemDTO : requestDTO.getItems()) {
            ConsumptionOrderItem orderItem = orderItemMap.get(itemDTO.getOrderItemId());

            if (orderItem == null) {
                errors.add("订单项不存在: " + itemDTO.getOrderItemId());
                continue;
            }

            int availableQty = orderItem.getQuantity() - orderItem.getReturnedQuantity();
            if (itemDTO.getQuantity() > availableQty) {
                errors.add("退货数量超过可退数量: " + orderItem.getProductName() +
                        "，可退: " + availableQty);
                continue;
            }

            BigDecimal subtotal = orderItem.getUnitPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalReturnAmount = totalReturnAmount.add(subtotal);

            ReturnItem returnItem = new ReturnItem();
            returnItem.setOrderItemId(orderItem.getId());
            returnItem.setProductId(orderItem.getProductId());
            returnItem.setProductName(orderItem.getProductName());
            returnItem.setUnitPrice(orderItem.getUnitPrice());
            returnItem.setQuantity(itemDTO.getQuantity());
            returnItem.setSubtotal(subtotal);
            returnItems.add(returnItem);
        }

        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("errors", errors);
            return result;
        }

        String returnNo = generateReturnNo();
        ReturnRecord returnRecord = new ReturnRecord();
        returnRecord.setReturnNo(returnNo);
        returnRecord.setOrderId(order.getId());
        returnRecord.setInmateId(order.getInmateId());
        returnRecord.setReturnAmount(totalReturnAmount);
        returnRecord.setReturnReason(requestDTO.getReturnReason());
        returnRecord.setStatus("待审批");
        ReturnRecord savedReturn = returnRecordRepository.save(returnRecord);

        for (ReturnItem item : returnItems) {
            item.setReturnRecord(savedReturn);
            returnItemRepository.save(item);
        }
        savedReturn.setItems(returnItems);

        result.put("success", true);
        result.put("returnRecord", savedReturn);
        return result;
    }

    @Transactional
    public ReturnRecord approveReturn(Long returnId, Long approverId, boolean approved, String rejectReason) {
        ReturnRecord returnRecord = returnRecordRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("退货记录不存在"));

        if (!"待审批".equals(returnRecord.getStatus())) {
            throw new RuntimeException("退货单状态不允许审批");
        }

        returnRecord.setApprovedBy(approverId);
        returnRecord.setApprovedAt(LocalDateTime.now());

        if (approved) {
            returnRecord.setStatus("已通过");
            processReturn(returnRecord);
        } else {
            returnRecord.setStatus("已拒绝");
            returnRecord.setRejectReason(rejectReason);
        }

        return returnRecordRepository.save(returnRecord);
    }

    @Transactional
    protected void processReturn(ReturnRecord returnRecord) {
        InmateAccount account = inmateAccountRepository.findByInmateId(returnRecord.getInmateId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        List<ReturnItem> items = returnItemRepository.findByReturnRecordId(returnRecord.getId());

        for (ReturnItem returnItem : items) {
            Product product = productRepository.findById(returnItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            product.setStockQuantity(product.getStockQuantity() + returnItem.getQuantity());
            productRepository.save(product);

            ConsumptionOrderItem orderItem = consumptionOrderItemRepository.findById(returnItem.getOrderItemId())
                    .orElseThrow(() -> new RuntimeException("订单项不存在"));
            orderItem.setReturnedQuantity(orderItem.getReturnedQuantity() + returnItem.getQuantity());
            consumptionOrderItemRepository.save(orderItem);
        }

        account.setBalance(account.getBalance().add(returnRecord.getReturnAmount()));
        inmateAccountRepository.save(account);
    }

    public ReturnRecord getReturnById(Long id) {
        return returnRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退货记录不存在"));
    }

    public ReturnRecord getReturnByNo(String returnNo) {
        return returnRecordRepository.findByReturnNo(returnNo)
                .orElseThrow(() -> new RuntimeException("退货记录不存在"));
    }

    public Page<ReturnRecord> listReturns(Long inmateId, String status, Pageable pageable) {
        if (inmateId != null) {
            return returnRecordRepository.findByInmateId(inmateId, pageable);
        }
        if (status != null && !status.isBlank()) {
            return returnRecordRepository.findByStatus(status, pageable);
        }
        return returnRecordRepository.findAll(pageable);
    }

    public List<ReturnItem> getReturnItems(Long returnId) {
        return returnItemRepository.findByReturnRecordId(returnId);
    }

    private String generateReturnNo() {
        return "RET" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
