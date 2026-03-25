package com.bylw.foodforum.dto.merchant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChefInfoSaveDTO {

    @NotBlank(message = "厨师姓名不能为空")
    @Size(max = 64, message = "厨师姓名长度不能超过64")
    private String chefName;

    @Size(max = 64, message = "头衔长度不能超过64")
    private String title;

    @Size(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;

    @Size(max = 500, message = "厨师介绍长度不能超过500")
    private String intro;

    private Integer sort;

    public String getChefName() {
        return chefName;
    }

    public void setChefName(String chefName) {
        this.chefName = chefName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
