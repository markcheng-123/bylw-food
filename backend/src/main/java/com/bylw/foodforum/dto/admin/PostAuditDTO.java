package com.bylw.foodforum.dto.admin;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class PostAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer result;

    @Size(max = 255, message = "审核备注不能超过255个字符")
    private String remark;
}
