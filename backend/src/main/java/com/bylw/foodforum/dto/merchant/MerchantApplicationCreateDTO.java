package com.bylw.foodforum.dto.merchant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantApplicationCreateDTO {

    @NotBlank(message = "商家名称不能为空")
    @Size(max = 128, message = "商家名称不能超过128个字符")
    private String merchantName;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人不能超过64个字符")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String contactPhone;

    @Size(max = 255, message = "营业执照地址不能超过255个字符")
    private String businessLicense;

    @Size(max = 32, message = "省份长度不能超过32个字符")
    private String province;

    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String detailAddress;

    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;

    @Size(max = 500, message = "申请说明不能超过500个字符")
    private String description;
}
