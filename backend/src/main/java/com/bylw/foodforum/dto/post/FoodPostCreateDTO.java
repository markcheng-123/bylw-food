package com.bylw.foodforum.dto.post;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FoodPostCreateDTO {

    @NotNull(message = "请选择分类")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不能超过120个字符")
    private String title;

    @Size(max = 255, message = "摘要不能超过255个字符")
    private String summary;

    @NotBlank(message = "美食描述不能为空")
    private String content;

    @Size(max = 255, message = "推荐地址不能超过255个字符")
    private String address;

    private Integer perCapita;

    @NotEmpty(message = "请至少上传一张图片")
    private List<@NotBlank(message = "图片地址不能为空") String> imageUrls;

    private List<@Valid PostDishDTO> dishes;

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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<PostDishDTO> getDishes() {
        return dishes;
    }

    public void setDishes(List<PostDishDTO> dishes) {
        this.dishes = dishes;
    }
}
