package com.bylw.foodforum.vo.merchant;

import java.time.LocalDateTime;
import java.util.List;

public class MerchantProfileVO {

    private Long id;
    private Long userId;
    private String merchantName;
    private String storeName;
    private String storeAddress;
    private Integer averageCost;
    private String businessLicenseUrl;
    private String foodSafetyCertUrl;
    private String chefTeamIntro;
    private Integer status;
    private String rejectReason;
    private LocalDateTime lastAuditedAt;
    private LocalDateTime updatedAt;
    private List<ChefInfoVO> chefs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getLastAuditedAt() {
        return lastAuditedAt;
    }

    public void setLastAuditedAt(LocalDateTime lastAuditedAt) {
        this.lastAuditedAt = lastAuditedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ChefInfoVO> getChefs() {
        return chefs;
    }

    public void setChefs(List<ChefInfoVO> chefs) {
        this.chefs = chefs;
    }
}
