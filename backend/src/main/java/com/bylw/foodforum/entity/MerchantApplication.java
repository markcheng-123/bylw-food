package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("merchant_application")
public class MerchantApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String merchantName;

    private String contactName;

    private String contactPhone;

    private String businessLicense;

    private String address;

    private String description;

    private Integer status;

    private String rejectReason;

    private LocalDateTime submittedAt;

    private LocalDateTime auditedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
