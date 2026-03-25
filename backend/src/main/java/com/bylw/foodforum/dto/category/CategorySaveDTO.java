package com.bylw.foodforum.dto.category;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CategorySaveDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过64个字符")
    private String name;

    private String icon;

    @NotNull(message = "排序值不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Size(max = 255, message = "分类说明不能超过255个字符")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
