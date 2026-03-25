package com.bylw.foodforum.dto.merchant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantApplicationAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer result;

    @Size(max = 255, message = "驳回原因不能超过255个字符")
    private String rejectReason;
}
