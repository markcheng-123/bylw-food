package com.bylw.foodforum.vo.admin;

import java.util.List;
import lombok.Data;

@Data
public class ProvinceMerchantItemVO {

    private String province;

    private long merchantCount;

    private List<String> topMerchants;
}
