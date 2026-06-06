package com.prison.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ConsumptionRequestDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<ConsumptionItemDTO> items;
}
