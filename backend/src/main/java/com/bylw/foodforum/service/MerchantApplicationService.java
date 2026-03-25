package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.dto.merchant.MerchantApplicationAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantApplicationCreateDTO;
import com.bylw.foodforum.entity.MerchantApplication;
import com.bylw.foodforum.vo.merchant.MerchantApplicationVO;
import java.util.List;

public interface MerchantApplicationService extends IService<MerchantApplication> {

    MerchantApplicationVO submitApplication(MerchantApplicationCreateDTO createDTO);

    List<MerchantApplicationVO> listCurrentUserApplications();

    List<MerchantApplicationVO> listAllApplicationsForAdmin(Integer status);

    MerchantApplicationVO auditApplication(Long id, MerchantApplicationAuditDTO auditDTO);
}
