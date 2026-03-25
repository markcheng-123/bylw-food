package com.bylw.foodforum.vo.admin;

import java.util.List;
import lombok.Data;

@Data
public class AdminMapVO {

    private List<String> highlightedProvinces;

    private List<ProvinceMerchantItemVO> provinceStats;
}
