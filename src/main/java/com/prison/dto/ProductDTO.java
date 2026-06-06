package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {

    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50")
    private String productCode;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100")
    private String name;

    @NotBlank(message = "商品类别不能为空")
    private String category;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    private BigDecimal unitPrice;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stockQuantity;

    @Min(value = 0, message = "限购数量不能为负数")
    private Integer monthlyLimitPerPerson;

    private String purchaseRestrictionLevel;

    private Boolean isOnSale;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
