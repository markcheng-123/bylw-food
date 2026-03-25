package com.bylw.foodforum.vo.merchant;

import java.util.List;

public class MerchantCardVO {

    private Long profileId;
    private Long merchantUserId;
    private String merchantName;
    private Integer status;
    private String storeName;
    private String storeAddress;
    private Integer averageCost;
    private String businessLicenseUrl;
    private String foodSafetyCertUrl;
    private String chefTeamIntro;
    private List<ChefInfoVO> chefs;
    private List<String> hotDishes;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getMerchantUserId() {
        return merchantUserId;
    }

    public void setMerchantUserId(Long merchantUserId) {
        this.merchantUserId = merchantUserId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public List<ChefInfoVO> getChefs() {
        return chefs;
    }

    public void setChefs(List<ChefInfoVO> chefs) {
        this.chefs = chefs;
    }

    public List<String> getHotDishes() {
        return hotDishes;
    }

    public void setHotDishes(List<String> hotDishes) {
        this.hotDishes = hotDishes;
    }
}
