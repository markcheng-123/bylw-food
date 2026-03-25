package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.dto.merchant.MerchantApplicationCreateDTO;
import com.bylw.foodforum.service.MerchantApplicationService;
import com.bylw.foodforum.vo.merchant.MerchantApplicationVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant-applications")
public class MerchantApplicationController {

    private final MerchantApplicationService merchantApplicationService;

    public MerchantApplicationController(MerchantApplicationService merchantApplicationService) {
        this.merchantApplicationService = merchantApplicationService;
    }

    @PostMapping
    public ApiResponse<MerchantApplicationVO> submitApplication(@Valid @RequestBody MerchantApplicationCreateDTO createDTO) {
        return ApiResponse.success("申请已提交", merchantApplicationService.submitApplication(createDTO));
    }

    @GetMapping("/mine")
    public ApiResponse<List<MerchantApplicationVO>> listMyApplications() {
        return ApiResponse.success(merchantApplicationService.listCurrentUserApplications());
    }
}
