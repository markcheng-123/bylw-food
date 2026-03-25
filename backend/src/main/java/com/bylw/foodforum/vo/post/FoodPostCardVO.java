package com.bylw.foodforum.vo.post;

import java.time.LocalDateTime;

public class FoodPostCardVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String coverImage;
    private String address;
    private Integer perCapita;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer heatScore;
    private Boolean likedByCurrentUser;
    private Boolean certifiedMerchant;
    private String merchantLabel;
    private Long merchantProfileId;
    private Long merchantUserId;
    private String merchantName;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getHeatScore() {
        return heatScore;
    }

    public void setHeatScore(Integer heatScore) {
        this.heatScore = heatScore;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Boolean getCertifiedMerchant() {
        return certifiedMerchant;
    }

    public void setCertifiedMerchant(Boolean certifiedMerchant) {
        this.certifiedMerchant = certifiedMerchant;
    }

    public String getMerchantLabel() {
        return merchantLabel;
    }

    public void setMerchantLabel(String merchantLabel) {
        this.merchantLabel = merchantLabel;
    }

    public Long getMerchantProfileId() {
        return merchantProfileId;
    }

    public void setMerchantProfileId(Long merchantProfileId) {
        this.merchantProfileId = merchantProfileId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
