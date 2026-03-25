package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.merchant.ChefInfoSaveDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileAuditDTO;
import com.bylw.foodforum.dto.merchant.MerchantProfileUpdateDTO;
import com.bylw.foodforum.entity.ChefInfo;
import com.bylw.foodforum.entity.FoodDish;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.entity.MerchantApplication;
import com.bylw.foodforum.entity.MerchantProfile;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.ChefInfoMapper;
import com.bylw.foodforum.mapper.FoodPostMapper;
import com.bylw.foodforum.mapper.MerchantApplicationMapper;
import com.bylw.foodforum.mapper.MerchantProfileMapper;
import com.bylw.foodforum.service.AuditRecordService;
import com.bylw.foodforum.service.FoodDishService;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.merchant.ChefInfoVO;
import com.bylw.foodforum.vo.merchant.MerchantCardVO;
import com.bylw.foodforum.vo.merchant.MerchantProfileVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MerchantProfileServiceImpl extends ServiceImpl<MerchantProfileMapper, MerchantProfile> implements MerchantProfileService {

    private final UserService userService;
    private final ChefInfoMapper chefInfoMapper;
    private final FoodPostMapper foodPostMapper;
    private final MerchantApplicationMapper merchantApplicationMapper;
    private final FoodDishService foodDishService;
    private final AuditRecordService auditRecordService;

    public MerchantProfileServiceImpl(
        UserService userService,
        ChefInfoMapper chefInfoMapper,
        FoodPostMapper foodPostMapper,
        MerchantApplicationMapper merchantApplicationMapper,
        FoodDishService foodDishService,
        AuditRecordService auditRecordService
    ) {
        this.userService = userService;
        this.chefInfoMapper = chefInfoMapper;
        this.foodPostMapper = foodPostMapper;
        this.merchantApplicationMapper = merchantApplicationMapper;
        this.foodDishService = foodDishService;
        this.auditRecordService = auditRecordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfile ensureMerchantProfile(Long userId) {
        MerchantProfile existed = getOne(new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, userId), false);
        if (existed != null) {
            return existed;
        }
        MerchantProfile profile = new MerchantProfile();
        profile.setUserId(userId);
        profile.setStatus(0);
        save(profile);
        return profile;
    }

    @Override
    public MerchantProfileVO getCurrentMerchantProfile() {
        MerchantProfile profile = ensureCurrentMerchantProfile();
        return toProfileVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfileVO updateCurrentMerchantProfile(MerchantProfileUpdateDTO updateDTO) {
        MerchantProfile profile = ensureCurrentMerchantProfile();

        boolean certChanged = certChanged(profile.getBusinessLicenseUrl(), updateDTO.getBusinessLicenseUrl())
            || certChanged(profile.getFoodSafetyCertUrl(), updateDTO.getFoodSafetyCertUrl());

        if (StringUtils.hasText(updateDTO.getMerchantName())) {
            profile.setMerchantName(updateDTO.getMerchantName().trim());
        }
        if (StringUtils.hasText(updateDTO.getStoreName())) {
            profile.setStoreName(updateDTO.getStoreName().trim());
        }
        if (StringUtils.hasText(updateDTO.getStoreAddress())) {
            profile.setStoreAddress(updateDTO.getStoreAddress().trim());
        }
        if (updateDTO.getAverageCost() != null && updateDTO.getAverageCost() >= 0) {
            profile.setAverageCost(updateDTO.getAverageCost());
        }
        profile.setBusinessLicenseUrl(emptyToNull(updateDTO.getBusinessLicenseUrl()));
        profile.setFoodSafetyCertUrl(emptyToNull(updateDTO.getFoodSafetyCertUrl()));
        profile.setChefTeamIntro(emptyToNull(updateDTO.getChefTeamIntro()));

        boolean resubmitForAudit = certChanged || Integer.valueOf(2).equals(profile.getStatus());
        if (resubmitForAudit) {
            profile.setStatus(0);
            profile.setRejectReason(null);
        }
        updateById(profile);

        if (resubmitForAudit) {
            auditRecordService.createRecord(
                "merchant_profile",
                profile.getId(),
                0,
                "Merchant submitted profile for re-audit",
                profile.getUserId()
            );
        }

        return toProfileVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfileVO auditProfile(Long profileId, MerchantProfileAuditDTO auditDTO) {
        User admin = ensureAdmin();
        MerchantProfile profile = getById(profileId);
        if (profile == null) {
            throw new BusinessException("Merchant profile not found");
        }

        boolean approved = Integer.valueOf(1).equals(auditDTO.getResult());
        profile.setStatus(approved ? 1 : 2);
        profile.setRejectReason(approved ? null : emptyToNull(auditDTO.getRejectReason()));
        profile.setLastAuditedAt(LocalDateTime.now());
        updateById(profile);

        auditRecordService.createRecord(
            "merchant_profile",
            profileId,
            approved ? 1 : 0,
            approved ? "Merchant profile approved" : profile.getRejectReason(),
            admin.getId()
        );

        return toProfileVO(profile);
    }

    @Override
    public List<MerchantProfileVO> listAllProfilesForAdmin(Integer status) {
        ensureAdmin();
        LambdaQueryWrapper<MerchantProfile> wrapper = new LambdaQueryWrapper<MerchantProfile>()
            .orderByDesc(MerchantProfile::getUpdatedAt)
            .orderByDesc(MerchantProfile::getId);
        if (status != null) {
            wrapper.eq(MerchantProfile::getStatus, status);
        }
        return list(wrapper).stream().map(this::toProfileVO).collect(Collectors.toList());
    }

    @Override
    public List<ChefInfoVO> listCurrentMerchantChefs() {
        MerchantProfile profile = ensureCurrentMerchantProfile();
        return toChefVOList(listChefEntities(profile.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChefInfoVO createChef(ChefInfoSaveDTO saveDTO) {
        MerchantProfile profile = ensureCurrentMerchantProfile();
        ChefInfo chef = new ChefInfo();
        chef.setMerchantProfileId(profile.getId());
        fillChefEntity(chef, saveDTO);
        chefInfoMapper.insert(chef);
        return toChefVO(chef);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChefInfoVO updateChef(Long chefId, ChefInfoSaveDTO saveDTO) {
        MerchantProfile profile = ensureCurrentMerchantProfile();
        ChefInfo chef = chefInfoMapper.selectById(chefId);
        if (chef == null || !profile.getId().equals(chef.getMerchantProfileId())) {
            throw new BusinessException(404, "Chef info not found");
        }
        fillChefEntity(chef, saveDTO);
        chefInfoMapper.updateById(chef);
        return toChefVO(chef);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChef(Long chefId) {
        MerchantProfile profile = ensureCurrentMerchantProfile();
        ChefInfo chef = chefInfoMapper.selectById(chefId);
        if (chef == null || !profile.getId().equals(chef.getMerchantProfileId())) {
            throw new BusinessException(404, "Chef info not found");
        }
        chefInfoMapper.deleteById(chefId);
    }

    @Override
    public MerchantCardVO getMerchantCardByUserId(Long merchantUserId) {
        MerchantProfile approvedProfile = getOne(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getUserId, merchantUserId)
            .eq(MerchantProfile::getStatus, 1), false);

        MerchantProfile profile = approvedProfile;
        if (profile == null) {
            profile = getOne(new LambdaQueryWrapper<MerchantProfile>()
                .eq(MerchantProfile::getUserId, merchantUserId), false);
        }

        MerchantCardVO cardVO = new MerchantCardVO();
        if (profile != null) {
            cardVO.setProfileId(profile.getId());
            cardVO.setMerchantUserId(profile.getUserId());
            cardVO.setMerchantName(profile.getMerchantName());
            cardVO.setStatus(profile.getStatus());
            cardVO.setStoreName(profile.getStoreName());
            cardVO.setStoreAddress(profile.getStoreAddress());
            cardVO.setAverageCost(profile.getAverageCost());
            cardVO.setBusinessLicenseUrl(profile.getBusinessLicenseUrl());
            cardVO.setFoodSafetyCertUrl(profile.getFoodSafetyCertUrl());
            cardVO.setChefTeamIntro(profile.getChefTeamIntro());
            cardVO.setChefs(toChefVOList(listChefEntities(profile.getId())));
            cardVO.setHotDishes(listHotDishNames(profile.getUserId()));
            return cardVO;
        }

        MerchantApplication application = merchantApplicationMapper.selectOne(new LambdaQueryWrapper<MerchantApplication>()
            .eq(MerchantApplication::getUserId, merchantUserId)
            .orderByDesc(MerchantApplication::getCreatedAt)
            .orderByDesc(MerchantApplication::getId)
            .last("LIMIT 1"));
        if (application == null) {
            throw new BusinessException(404, "Merchant info not found");
        }

        cardVO.setProfileId(null);
        cardVO.setMerchantUserId(application.getUserId());
        cardVO.setMerchantName(application.getMerchantName());
        cardVO.setStatus(application.getStatus() == null ? 0 : application.getStatus());
        cardVO.setStoreName(application.getMerchantName());
        cardVO.setStoreAddress(application.getAddress());
        cardVO.setAverageCost(null);
        cardVO.setBusinessLicenseUrl(application.getBusinessLicense());
        cardVO.setFoodSafetyCertUrl(null);
        cardVO.setChefTeamIntro(null);
        cardVO.setChefs(new ArrayList<ChefInfoVO>());
        cardVO.setHotDishes(listHotDishNames(application.getUserId()));
        return cardVO;
    }

    @Override
    public Map<Long, MerchantProfile> mapApprovedProfilesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<Long, MerchantProfile>();
        }
        return list(new LambdaQueryWrapper<MerchantProfile>()
            .in(MerchantProfile::getUserId, userIds)
            .eq(MerchantProfile::getStatus, 1))
            .stream()
            .collect(Collectors.toMap(MerchantProfile::getUserId, item -> item, (left, right) -> left, HashMap::new));
    }

    private MerchantProfile ensureCurrentMerchantProfile() {
        User currentUser = userService.getCurrentUserEntity();
        boolean merchantRole = currentUser.getRole() != null && currentUser.getRole() == 2;
        boolean hasMerchantApplication = merchantApplicationMapper.selectCount(new LambdaQueryWrapper<MerchantApplication>()
            .eq(MerchantApplication::getUserId, currentUser.getId())) > 0;
        if (!merchantRole && !hasMerchantApplication) {
            throw new BusinessException(403, "Current account has not submitted merchant application");
        }
        return ensureMerchantProfile(currentUser.getId());
    }

    private User ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "No admin permission");
        }
        return currentUser;
    }

    private MerchantProfileVO toProfileVO(MerchantProfile profile) {
        MerchantProfileVO vo = new MerchantProfileVO();
        BeanUtils.copyProperties(profile, vo);
        vo.setChefs(toChefVOList(listChefEntities(profile.getId())));
        return vo;
    }

    private List<ChefInfo> listChefEntities(Long merchantProfileId) {
        return chefInfoMapper.selectList(new LambdaQueryWrapper<ChefInfo>()
            .eq(ChefInfo::getMerchantProfileId, merchantProfileId)
            .orderByAsc(ChefInfo::getSort)
            .orderByDesc(ChefInfo::getId));
    }

    private List<ChefInfoVO> toChefVOList(List<ChefInfo> chefs) {
        if (chefs == null || chefs.isEmpty()) {
            return new ArrayList<ChefInfoVO>();
        }
        return chefs.stream().map(this::toChefVO).collect(Collectors.toList());
    }

    private ChefInfoVO toChefVO(ChefInfo chef) {
        ChefInfoVO vo = new ChefInfoVO();
        BeanUtils.copyProperties(chef, vo);
        return vo;
    }

    private void fillChefEntity(ChefInfo chef, ChefInfoSaveDTO saveDTO) {
        chef.setChefName(saveDTO.getChefName().trim());
        chef.setTitle(emptyToNull(saveDTO.getTitle()));
        chef.setAvatarUrl(emptyToNull(saveDTO.getAvatarUrl()));
        chef.setIntro(emptyToNull(saveDTO.getIntro()));
        chef.setSort(saveDTO.getSort() == null ? 0 : saveDTO.getSort());
        chef.setStatus(1);
    }

    private List<String> listHotDishNames(Long merchantUserId) {
        List<FoodPost> postList = foodPostMapper.selectList(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getAuthorUserId, merchantUserId)
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getLikeCount)
            .orderByDesc(FoodPost::getViewCount)
            .orderByDesc(FoodPost::getId)
            .last("LIMIT 8"));
        if (postList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> postIds = postList.stream().map(FoodPost::getId).collect(Collectors.toList());
        List<FoodDish> dishes = foodDishService.list(new LambdaQueryWrapper<FoodDish>()
            .in(FoodDish::getPostId, postIds)
            .orderByAsc(FoodDish::getSort)
            .orderByDesc(FoodDish::getId));

        Set<String> names = new LinkedHashSet<String>();
        for (FoodDish dish : dishes) {
            if (StringUtils.hasText(dish.getDishName())) {
                names.add(dish.getDishName().trim());
            }
            if (names.size() >= 8) {
                break;
            }
        }
        return new ArrayList<String>(names);
    }

    private boolean certChanged(String oldValue, String newValue) {
        String oldTrimmed = emptyToNull(oldValue);
        String newTrimmed = emptyToNull(newValue);
        if (oldTrimmed == null) {
            return newTrimmed != null;
        }
        return !oldTrimmed.equals(newTrimmed);
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
