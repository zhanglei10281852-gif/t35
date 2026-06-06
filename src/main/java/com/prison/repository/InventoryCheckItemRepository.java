package com.prison.repository;

import com.prison.entity.InventoryCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryCheckItemRepository extends JpaRepository<InventoryCheckItem, Long> {

    List<InventoryCheckItem> findByInventoryCheckId(Long checkId);
}
