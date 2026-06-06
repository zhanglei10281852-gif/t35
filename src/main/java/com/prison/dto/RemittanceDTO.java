package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RemittanceDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotBlank(message = "汇款人姓名不能为空")
    @Size(max = 50, message = "汇款人姓名长度不能超过50")
    private String remitterName;

    @NotBlank(message = "与服刑人员关系不能为空")
    @Size(max = 20, message = "关系长度不能超过20")
    private String relationship;

    @NotNull(message = "汇款金额不能为空")
    @DecimalMin(value = "0.01", message = "汇款金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "汇款日期不能为空")
    private String remittanceDate;

    private String sourceType;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
