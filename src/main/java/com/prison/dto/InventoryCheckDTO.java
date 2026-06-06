package com.prison.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class InventoryCheckDTO {

    @NotBlank(message = "盘点日期不能为空")
    private String checkDate;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    @NotEmpty(message = "盘点商品列表不能为空")
    @Valid
    private List<InventoryCheckItemDTO> items;

    private Long operatedBy;
}
