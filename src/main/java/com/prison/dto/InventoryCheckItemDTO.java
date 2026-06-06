package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InventoryCheckItemDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "实际盘点数量不能为空")
    @Min(value = 0, message = "实际盘点数量不能为负数")
    private Integer actualQuantity;
}
