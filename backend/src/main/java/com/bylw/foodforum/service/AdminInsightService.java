package com.bylw.foodforum.service;

import com.bylw.foodforum.vo.admin.AdminDataCenterVO;
import com.bylw.foodforum.vo.admin.AdminMapVO;

public interface AdminInsightService {

    AdminDataCenterVO getDataCenterInsights();

    AdminMapVO getMapInsights();
}
