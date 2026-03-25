package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.merchant.MerchantApplicationAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantApplicationCreateDTO;
import com.bylw.foodforum.entity.MerchantApplication;
import com.bylw.foodforum.entity.MerchantProfile;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.MerchantApplicationMapper;
import com.bylw.foodforum.service.AuditRecordService;
import com.bylw.foodforum.service.MerchantApplicationService;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.merchant.MerchantApplicationVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MerchantApplicationServiceImpl extends ServiceImpl<MerchantApplicationMapper, MerchantApplication> implements MerchantApplicationService {

    private final UserService userService;
    private final AuditRecordService auditRecordService;
    private final MerchantProfileService merchantProfileService;

    public MerchantApplicationServiceImpl(
        UserService userService,
        AuditRecordService auditRecordService,
        MerchantProfileService merchantProfileService
    ) {
        this.userService = userService;
        this.auditRecordService = auditRecordService;
        this.merchantProfileService = merchantProfileService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplicationVO submitApplication(MerchantApplicationCreateDTO createDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "Please login first");
        }

        MerchantApplication application = new MerchantApplication();
        application.setUserId(currentUserId);
        application.setMerchantName(createDTO.getMerchantName().trim());
        application.setContactName(createDTO.getContactName().trim());
        application.setContactPhone(createDTO.getContactPhone().trim());
        application.setBusinessLicense(emptyToNull(createDTO.getBusinessLicense()));
        application.setAddress(resolveAddress(createDTO));
        application.setDescription(emptyToNull(createDTO.getDescription()));
        application.setStatus(0);
        application.setRejectReason(null);
        application.setSubmittedAt(LocalDateTime.now());
        save(application);
        return toVO(application, userService.getById(currentUserId));
    }

    @Override
    public List<MerchantApplicationVO> listCurrentUserApplications() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "Please login first");
        }
        List<MerchantApplication> applications = list(new LambdaQueryWrapper<MerchantApplication>()
            .eq(MerchantApplication::getUserId, currentUserId)
            .orderByDesc(MerchantApplication::getCreatedAt)
            .orderByDesc(MerchantApplication::getId));
        User currentUser = userService.getById(currentUserId);
        return applications.stream().map(item -> toVO(item, currentUser)).collect(Collectors.toList());
    }

    @Override
    public List<MerchantApplicationVO> listAllApplicationsForAdmin(Integer status) {
        ensureAdmin();
        LambdaQueryWrapper<MerchantApplication> wrapper = new LambdaQueryWrapper<MerchantApplication>()
            .orderByDesc(MerchantApplication::getCreatedAt)
            .orderByDesc(MerchantApplication::getId);
        if (status != null) {
            wrapper.eq(MerchantApplication::getStatus, status);
        }
        List<MerchantApplication> applications = list(wrapper);
        if (applications.isEmpty()) {
            return new ArrayList<MerchantApplicationVO>();
        }

        List<Long> userIds = applications.stream().map(MerchantApplication::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left, HashMap::new));

        return applications.stream()
            .map(item -> toVO(item, userMap.get(item.getUserId())))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplicationVO auditApplication(Long id, MerchantApplicationAuditDTO auditDTO) {
        User admin = ensureAdmin();

        MerchantApplication application = getById(id);
        if (application == null) {
            throw new BusinessException("Application record not found");
        }

        boolean approved = Integer.valueOf(1).equals(auditDTO.getResult());
        application.setStatus(approved ? 1 : 2);
        application.setAuditedAt(LocalDateTime.now());
        application.setRejectReason(approved ? null : emptyToNull(auditDTO.getRejectReason()));
        updateById(application);

        if (approved) {
            User applicant = userService.getById(application.getUserId());
            if (applicant != null && (applicant.getRole() == null || applicant.getRole() != 9)) {
                applicant.setRole(2);
                userService.updateById(applicant);
            }

            MerchantProfile merchantProfile = merchantProfileService.ensureMerchantProfile(application.getUserId());
            merchantProfile.setMerchantName(application.getMerchantName());
            merchantProfile.setStoreName(application.getMerchantName());
            merchantProfile.setStoreAddress(application.getAddress());
            merchantProfile.setBusinessLicenseUrl(application.getBusinessLicense());
            merchantProfile.setStatus(1);
            merchantProfile.setRejectReason(null);
            merchantProfile.setLastAuditedAt(LocalDateTime.now());
            merchantProfileService.updateById(merchantProfile);
        }

        auditRecordService.createRecord(
            "merchant_application",
            id,
            approved ? 1 : 0,
            approved ? "Merchant application approved" : application.getRejectReason(),
            admin.getId()
        );

        return toVO(application, userService.getById(application.getUserId()));
    }

    private MerchantApplicationVO toVO(MerchantApplication application, User applicant) {
        MerchantApplicationVO vo = new MerchantApplicationVO();
        BeanUtils.copyProperties(application, vo);
        vo.setApplicantNickname(applicant == null ? "User" : applicant.getNickname());
        return vo;
    }

    private User ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "No admin permission");
        }
        return currentUser;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveAddress(MerchantApplicationCreateDTO createDTO) {
        String province = emptyToNull(createDTO.getProvince());
        String detailAddress = emptyToNull(createDTO.getDetailAddress());
        if (StringUtils.hasText(province) && StringUtils.hasText(detailAddress)) {
            return province + " " + detailAddress;
        }

        String address = emptyToNull(createDTO.getAddress());
        if (StringUtils.hasText(address)) {
            return address;
        }

        throw new BusinessException("Address is required");
    }
}
