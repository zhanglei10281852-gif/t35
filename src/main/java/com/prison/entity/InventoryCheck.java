package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "inventory_checks")
public class InventoryCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String checkNo;

    @Column(nullable = false)
    private LocalDate checkDate;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBookValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalActualValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal profitAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lossAmount;

    @Column(nullable = false, length = 20)
    private String status;

    private Long operatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "inventoryCheck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryCheckItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "已完成";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
