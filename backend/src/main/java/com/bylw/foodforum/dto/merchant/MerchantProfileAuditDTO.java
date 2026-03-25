package com.bylw.foodforum.dto.merchant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MerchantProfileAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer result;

    @Size(max = 255, message = "驳回原因长度不能超过255")
    private String rejectReason;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
