package com.bylw.foodforum.vo.merchant;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MerchantApplicationVO {

    private Long id;
    private Long userId;
    private String applicantNickname;
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
}
