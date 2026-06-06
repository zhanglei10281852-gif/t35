package com.prison.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ReturnRequestDTO {

    @NotNull(message = "原订单ID不能为空")
    private Long orderId;

    @NotEmpty(message = "退货商品列表不能为空")
    @Valid
    private List<ReturnItemDTO> items;

    @NotBlank(message = "退货原因不能为空")
    @Size(max = 500, message = "退货原因长度不能超过500")
    private String returnReason;
}
