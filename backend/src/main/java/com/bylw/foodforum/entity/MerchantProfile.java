package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("merchant_profile")
public class MerchantProfile {

    @TableId(type = IdType.AUTO)
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
