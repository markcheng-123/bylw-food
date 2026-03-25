package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.dto.merchant.ChefInfoSaveDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileUpdateDTO;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.vo.merchant.ChefInfoVO;
import com.bylw.foodforum.vo.merchant.MerchantCardVO;
import com.bylw.foodforum.vo.merchant.MerchantProfileVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/merchant-profiles")
public class MerchantProfileController {

    private final MerchantProfileService merchantProfileService;

    public MerchantProfileController(MerchantProfileService merchantProfileService) {
        this.merchantProfileService = merchantProfileService;
    }

    @GetMapping("/me")
    public ApiResponse<MerchantProfileVO> getMyProfile() {
        return ApiResponse.success(merchantProfileService.getCurrentMerchantProfile());
    }

    @PutMapping("/me")
    public ApiResponse<MerchantProfileVO> updateMyProfile(@Valid @RequestBody MerchantProfileUpdateDTO updateDTO) {
        return ApiResponse.success("商家资料更新成功", merchantProfileService.updateCurrentMerchantProfile(updateDTO));
    }

    @GetMapping("/me/chefs")
    public ApiResponse<List<ChefInfoVO>> listMyChefs() {
        return ApiResponse.success(merchantProfileService.listCurrentMerchantChefs());
    }

    @PostMapping("/me/chefs")
    public ApiResponse<ChefInfoVO> createChef(@Valid @RequestBody ChefInfoSaveDTO saveDTO) {
        return ApiResponse.success("厨师信息添加成功", merchantProfileService.createChef(saveDTO));
    }

    @PutMapping("/me/chefs/{chefId}")
    public ApiResponse<ChefInfoVO> updateChef(@PathVariable Long chefId, @Valid @RequestBody ChefInfoSaveDTO saveDTO) {
        return ApiResponse.success("厨师信息更新成功", merchantProfileService.updateChef(chefId, saveDTO));
    }

    @DeleteMapping("/me/chefs/{chefId}")
    public ApiResponse<Void> deleteChef(@PathVariable Long chefId) {
        merchantProfileService.deleteChef(chefId);
        return ApiResponse.success("厨师信息已删除", null);
    }

    @GetMapping("/card/{merchantUserId}")
    public ApiResponse<MerchantCardVO> getMerchantCard(@PathVariable Long merchantUserId) {
        return ApiResponse.success(merchantProfileService.getMerchantCardByUserId(merchantUserId));
    }
}
