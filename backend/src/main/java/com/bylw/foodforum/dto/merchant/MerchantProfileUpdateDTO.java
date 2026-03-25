package com.bylw.foodforum.dto.merchant;

import javax.validation.constraints.Size;

public class MerchantProfileUpdateDTO {

    @Size(max = 128, message = "商家名称长度不能超过128")
    private String merchantName;

    @Size(max = 128, message = "门店名称长度不能超过128")
    private String storeName;

    @Size(max = 255, message = "门店地址长度不能超过255")
    private String storeAddress;

    private Integer averageCost;

    @Size(max = 255, message = "营业执照图片地址过长")
    private String businessLicenseUrl;

    @Size(max = 255, message = "食品健康安全证图片地址过长")
    private String foodSafetyCertUrl;

    @Size(max = 500, message = "厨师团队介绍长度不能超过500")
    private String chefTeamIntro;

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public Integer getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(Integer averageCost) {
        this.averageCost = averageCost;
    }

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }

    public String getFoodSafetyCertUrl() {
        return foodSafetyCertUrl;
    }

    public void setFoodSafetyCertUrl(String foodSafetyCertUrl) {
        this.foodSafetyCertUrl = foodSafetyCertUrl;
    }

    public String getChefTeamIntro() {
        return chefTeamIntro;
    }

    public void setChefTeamIntro(String chefTeamIntro) {
        this.chefTeamIntro = chefTeamIntro;
    }
}
