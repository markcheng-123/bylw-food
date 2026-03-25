package com.bylw.foodforum.dto.category;

import javax.validation.constraints.NotNull;

public class CategoryStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
