package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReturnItemDTO {

    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    @NotNull(message = "退货数量不能为空")
    @Min(value = 1, message = "退货数量至少为1")
    private Integer quantity;
}
