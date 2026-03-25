package com.bylw.foodforum.dto.post;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FoodPostUpdateDTO {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title is too long")
    private String title;

    @Size(max = 255, message = "Summary is too long")
    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 255, message = "Address is too long")
    private String address;

    private Integer perCapita;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPerCapita() {
        return perCapita;
    }

    public void setPerCapita(Integer perCapita) {
        this.perCapita = perCapita;
    }
}
