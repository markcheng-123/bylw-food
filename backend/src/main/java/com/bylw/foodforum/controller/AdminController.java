package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.dto.admin.PostAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantApplicationAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileAuditDTO;
import com.bylw.foodforum.service.AdminInsightService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.ImageMigrationService;
import com.bylw.foodforum.service.MerchantApplicationService;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.vo.admin.AdminDataCenterVO;
import com.bylw.foodforum.vo.admin.AdminMapVO;
import com.bylw.foodforum.vo.admin.AdminPostVO;
import com.bylw.foodforum.vo.admin.ImageMigrationResultVO;
import com.bylw.foodforum.vo.merchant.MerchantApplicationVO;
import com.bylw.foodforum.vo.merchant.MerchantProfileVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final FoodPostService foodPostService;
    private final MerchantApplicationService merchantApplicationService;
    private final MerchantProfileService merchantProfileService;
    private final AdminInsightService adminInsightService;
    private final ImageMigrationService imageMigrationService;

    public AdminController(
        FoodPostService foodPostService,
        MerchantApplicationService merchantApplicationService,
        MerchantProfileService merchantProfileService,
        AdminInsightService adminInsightService,
        ImageMigrationService imageMigrationService
    ) {
        this.foodPostService = foodPostService;
        this.merchantApplicationService = merchantApplicationService;
        this.merchantProfileService = merchantProfileService;
        this.adminInsightService = adminInsightService;
        this.imageMigrationService = imageMigrationService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResult<AdminPostVO>> listPosts(
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(foodPostService.queryPostsForAdmin(current, size, status));
    }

    @GetMapping("/insights/data-center")
    public ApiResponse<AdminDataCenterVO> getDataCenterInsights() {
        return ApiResponse.success(adminInsightService.getDataCenterInsights());
    }

    @GetMapping("/insights/map")
    public ApiResponse<AdminMapVO> getMapInsights() {
        return ApiResponse.success(adminInsightService.getMapInsights());
    }

    @PutMapping("/tools/migrate-local-images")
    public ApiResponse<ImageMigrationResultVO> migrateLocalImagesToOss() {
        return ApiResponse.success("Local images migrated to OSS", imageMigrationService.migrateLocalUploadsToOss());
    }

    @PutMapping("/posts/{id}/audit")
    public ApiResponse<Void> auditPost(@PathVariable Long id, @Valid @RequestBody PostAuditDTO auditDTO) {
        foodPostService.auditPost(id, auditDTO);
        return ApiResponse.success("Post reviewed", null);
    }

    @GetMapping("/merchant-applications")
    public ApiResponse<List<MerchantApplicationVO>> listMerchantApplications(@RequestParam(required = false) Integer status) {
        return ApiResponse.success(merchantApplicationService.listAllApplicationsForAdmin(status));
    }

    @PutMapping("/merchant-applications/{id}/audit")
    public ApiResponse<MerchantApplicationVO> auditMerchantApplication(
        @PathVariable Long id,
        @Valid @RequestBody MerchantApplicationAuditDTO auditDTO
    ) {
        return ApiResponse.success("Merchant application reviewed", merchantApplicationService.auditApplication(id, auditDTO));
    }

    @GetMapping("/merchant-profiles")
    public ApiResponse<List<MerchantProfileVO>> listMerchantProfiles(@RequestParam(required = false) Integer status) {
        return ApiResponse.success(merchantProfileService.listAllProfilesForAdmin(status));
    }

    @PutMapping("/merchant-profiles/{id}/audit")
    public ApiResponse<MerchantProfileVO> auditMerchantProfile(
        @PathVariable Long id,
        @Valid @RequestBody MerchantProfileAuditDTO auditDTO
    ) {
        return ApiResponse.success("Merchant profile reviewed", merchantProfileService.auditProfile(id, auditDTO));
    }
}
