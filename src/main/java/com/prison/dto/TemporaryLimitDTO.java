package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TemporaryLimitDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotNull(message = "调整后额度不能为空")
    @DecimalMin(value = "0.01", message = "调整后额度必须大于0")
    private BigDecimal adjustedLimit;

    @NotBlank(message = "生效日期不能为空")
    private String effectiveDate;

    @NotBlank(message = "失效日期不能为空")
    private String expiryDate;

    @NotBlank(message = "申请原因不能为空")
    @Size(max = 500, message = "申请原因长度不能超过500")
    private String reason;

    private Long appliedBy;
}
