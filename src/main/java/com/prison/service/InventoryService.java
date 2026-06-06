package com.prison.service;

import com.prison.dto.InventoryCheckDTO;
import com.prison.dto.InventoryCheckItemDTO;
import com.prison.entity.InventoryCheck;
import com.prison.entity.InventoryCheckItem;
import com.prison.entity.Product;
import com.prison.repository.InventoryCheckItemRepository;
import com.prison.repository.InventoryCheckRepository;
import com.prison.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryCheckRepository inventoryCheckRepository;
    private final InventoryCheckItemRepository inventoryCheckItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public InventoryCheck createInventoryCheck(InventoryCheckDTO dto) {
        String checkNo = generateCheckNo();
        LocalDate checkDate = LocalDate.parse(dto.getCheckDate());

        InventoryCheck check = new InventoryCheck();
        check.setCheckNo(checkNo);
        check.setCheckDate(checkDate);
        check.setRemark(dto.getRemark());
        check.setOperatedBy(dto.getOperatedBy());
        check.setStatus("已完成");

        BigDecimal totalBookValue = BigDecimal.ZERO;
        BigDecimal totalActualValue = BigDecimal.ZERO;
        BigDecimal profitAmount = BigDecimal.ZERO;
        BigDecimal lossAmount = BigDecimal.ZERO;

        List<InventoryCheckItem> checkItems = new ArrayList<>();

        for (InventoryCheckItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + itemDTO.getProductId()));

            int bookQty = product.getStockQuantity();
            int actualQty = itemDTO.getActualQuantity();
            int diffQty = actualQty - bookQty;
            BigDecimal diffAmount = product.getUnitPrice().multiply(new BigDecimal(diffQty));

            InventoryCheckItem checkItem = new InventoryCheckItem();
            checkItem.setProductId(product.getId());
            checkItem.setProductName(product.getName());
            checkItem.setBookQuantity(bookQty);
            checkItem.setActualQuantity(actualQty);
            checkItem.setDifferenceQuantity(diffQty);
            checkItem.setUnitPrice(product.getUnitPrice());
            checkItem.setDifferenceAmount(diffAmount);

            if (diffQty > 0) {
                checkItem.setDifferenceType("盘盈");
                profitAmount = profitAmount.add(diffAmount);
            } else if (diffQty < 0) {
                checkItem.setDifferenceType("盘亏");
                lossAmount = lossAmount.add(diffAmount.abs());
            } else {
                checkItem.setDifferenceType("正常");
            }

            totalBookValue = totalBookValue.add(product.getUnitPrice().multiply(new BigDecimal(bookQty)));
            totalActualValue = totalActualValue.add(product.getUnitPrice().multiply(new BigDecimal(actualQty)));

            checkItems.add(checkItem);
        }

        check.setTotalBookValue(totalBookValue);
        check.setTotalActualValue(totalActualValue);
        check.setProfitAmount(profitAmount);
        check.setLossAmount(lossAmount);

        InventoryCheck savedCheck = inventoryCheckRepository.save(check);

        for (InventoryCheckItem item : checkItems) {
            item.setInventoryCheck(savedCheck);
            inventoryCheckItemRepository.save(item);
        }
        savedCheck.setItems(checkItems);

        return savedCheck;
    }

    public InventoryCheck getInventoryCheckById(Long id) {
        return inventoryCheckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点记录不存在"));
    }

    public InventoryCheck getInventoryCheckByNo(String checkNo) {
        return inventoryCheckRepository.findByCheckNo(checkNo)
                .orElseThrow(() -> new RuntimeException("盘点记录不存在"));
    }

    public Page<InventoryCheck> listInventoryChecks(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            return inventoryCheckRepository.findByCheckDateBetween(startDate, endDate, pageable);
        }
        return inventoryCheckRepository.findAll(pageable);
    }

    public List<InventoryCheckItem> getCheckItems(Long checkId) {
        return inventoryCheckItemRepository.findByInventoryCheckId(checkId);
    }

    @Transactional
    public void adjustStockByCheck(Long checkId) {
        InventoryCheck check = inventoryCheckRepository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("盘点记录不存在"));

        List<InventoryCheckItem> items = inventoryCheckItemRepository.findByInventoryCheckId(checkId);
        for (InventoryCheckItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            product.setStockQuantity(item.getActualQuantity());
            productRepository.save(product);
        }
    }

    public List<InventoryCheck> getMonthlyChecks(String month) {
        return inventoryCheckRepository.findByMonth(month);
    }

    private String generateCheckNo() {
        return "INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
