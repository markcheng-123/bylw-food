package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.dto.merchant.ChefInfoSaveDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileUpdateDTO;
import com.bylw.foodforum.entity.MerchantProfile;
import com.bylw.foodforum.vo.merchant.ChefInfoVO;
import com.bylw.foodforum.vo.merchant.MerchantCardVO;
import com.bylw.foodforum.vo.merchant.MerchantProfileVO;
import java.util.List;
import java.util.Map;

public interface MerchantProfileService extends IService<MerchantProfile> {

    MerchantProfile ensureMerchantProfile(Long userId);

    MerchantProfileVO getCurrentMerchantProfile();

    MerchantProfileVO updateCurrentMerchantProfile(MerchantProfileUpdateDTO updateDTO);

    MerchantProfileVO auditProfile(Long profileId, MerchantProfileAuditDTO auditDTO);

    List<MerchantProfileVO> listAllProfilesForAdmin(Integer status);

    List<ChefInfoVO> listCurrentMerchantChefs();

    ChefInfoVO createChef(ChefInfoSaveDTO saveDTO);

    ChefInfoVO updateChef(Long chefId, ChefInfoSaveDTO saveDTO);

    void deleteChef(Long chefId);

    MerchantCardVO getMerchantCardByUserId(Long merchantUserId);

    Map<Long, MerchantProfile> mapApprovedProfilesByUserIds(List<Long> userIds);
}
