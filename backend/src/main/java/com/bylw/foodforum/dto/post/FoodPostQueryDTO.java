package com.bylw.foodforum.dto.post;

import javax.validation.constraints.Min;

public class FoodPostQueryDTO {

    private Long categoryId;
    private String keyword;

    @Min(value = 1, message = "页码不能小于1")
    private long current = 1L;

    @Min(value = 1, message = "每页数量不能小于1")
    private long size = 9L;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
