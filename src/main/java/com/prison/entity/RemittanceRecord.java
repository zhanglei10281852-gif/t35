package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "remittance_records")
public class RemittanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false, length = 50)
    private String remitterName;

    @Column(nullable = false, length = 20)
    private String relationship;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate remittanceDate;

    @Column(length = 20)
    private String sourceType;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "已到账";
        }
        if (this.sourceType == null) {
            this.sourceType = "家属汇款";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
