package com.bylw.foodforum.vo.admin;

import java.util.List;
import lombok.Data;

@Data
public class AdminDataCenterVO {

    private List<ActiveTrendItemVO> activeTrend;

    private List<CategoryShareItemVO> categoryShare;

    private List<MerchantCityItemVO> merchantCities;
}
