package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.entity.FoodCategory;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.entity.MerchantProfile;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.service.AdminInsightService;
import com.bylw.foodforum.service.FoodCategoryService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.admin.ActiveTrendItemVO;
import com.bylw.foodforum.vo.admin.AdminDataCenterVO;
import com.bylw.foodforum.vo.admin.AdminMapVO;
import com.bylw.foodforum.vo.admin.CategoryShareItemVO;
import com.bylw.foodforum.vo.admin.MerchantCityItemVO;
import com.bylw.foodforum.vo.admin.ProvinceMerchantItemVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminInsightServiceImpl implements AdminInsightService {

    private static final String OTHER = "其他";
    private static final Set<String> CANONICAL_PROVINCES = new HashSet<String>(Arrays.asList(
        "北京市", "天津市", "上海市", "重庆市",
        "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省",
        "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
        "河南省", "湖北省", "湖南省", "广东省", "海南省",
        "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省", "台湾省",
        "内蒙古自治区", "广西壮族自治区", "宁夏回族自治区", "新疆维吾尔自治区", "西藏自治区",
        "香港特别行政区", "澳门特别行政区"
    ));
    private static final List<String> MUNICIPALITIES = Arrays.asList("北京市", "天津市", "上海市", "重庆市");
    private static final Map<String, String> ALIASES = new HashMap<String, String>();
    private static final Map<String, String> CITY_TO_PROVINCE = new HashMap<String, String>();

    private static final Pattern PROVINCE_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{2,6}省|[\\u4e00-\\u9fa5]{2,8}自治区|[\\u4e00-\\u9fa5]{2,8}特别行政区|北京市|天津市|上海市|重庆市)"
    );
    private static final Pattern CITY_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5]{2,12}(市|州|地区|盟))");

    static {
        ALIASES.put("北京", "北京市");
        ALIASES.put("天津", "天津市");
        ALIASES.put("上海", "上海市");
        ALIASES.put("重庆", "重庆市");
        ALIASES.put("河北", "河北省");
        ALIASES.put("山西", "山西省");
        ALIASES.put("辽宁", "辽宁省");
        ALIASES.put("吉林", "吉林省");
        ALIASES.put("黑龙江", "黑龙江省");
        ALIASES.put("江苏", "江苏省");
        ALIASES.put("浙江", "浙江省");
        ALIASES.put("安徽", "安徽省");
        ALIASES.put("福建", "福建省");
        ALIASES.put("江西", "江西省");
        ALIASES.put("山东", "山东省");
        ALIASES.put("河南", "河南省");
        ALIASES.put("湖北", "湖北省");
        ALIASES.put("湖南", "湖南省");
        ALIASES.put("广东", "广东省");
        ALIASES.put("海南", "海南省");
        ALIASES.put("四川", "四川省");
        ALIASES.put("贵州", "贵州省");
        ALIASES.put("云南", "云南省");
        ALIASES.put("陕西", "陕西省");
        ALIASES.put("甘肃", "甘肃省");
        ALIASES.put("青海", "青海省");
        ALIASES.put("台湾", "台湾省");
        ALIASES.put("内蒙古", "内蒙古自治区");
        ALIASES.put("广西", "广西壮族自治区");
        ALIASES.put("宁夏", "宁夏回族自治区");
        ALIASES.put("新疆", "新疆维吾尔自治区");
        ALIASES.put("西藏", "西藏自治区");
        ALIASES.put("香港", "香港特别行政区");
        ALIASES.put("澳门", "澳门特别行政区");

        CITY_TO_PROVINCE.put("武汉", "湖北省");
        CITY_TO_PROVINCE.put("武昌", "湖北省");
        CITY_TO_PROVINCE.put("汉口", "湖北省");
        CITY_TO_PROVINCE.put("汉阳", "湖北省");
    }

    private final UserService userService;
    private final FoodPostService foodPostService;
    private final FoodCategoryService foodCategoryService;
    private final MerchantProfileService merchantProfileService;

    public AdminInsightServiceImpl(
        UserService userService,
        FoodPostService foodPostService,
        FoodCategoryService foodCategoryService,
        MerchantProfileService merchantProfileService
    ) {
        this.userService = userService;
        this.foodPostService = foodPostService;
        this.foodCategoryService = foodCategoryService;
        this.merchantProfileService = merchantProfileService;
    }

    @Override
    public AdminDataCenterVO getDataCenterInsights() {
        ensureAdmin();
        AdminDataCenterVO vo = new AdminDataCenterVO();
        vo.setActiveTrend(buildActiveTrend());
        vo.setCategoryShare(buildCategoryShare());
        vo.setMerchantCities(buildMerchantCities());
        return vo;
    }

    @Override
    public AdminMapVO getMapInsights() {
        ensureAdmin();
        List<MerchantProfile> approvedProfiles = merchantProfileService.list(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getStatus, 1)
            .orderByDesc(MerchantProfile::getUpdatedAt)
            .orderByDesc(MerchantProfile::getId));

        Map<String, List<MerchantProfile>> provinceGrouped = new LinkedHashMap<String, List<MerchantProfile>>();
        for (MerchantProfile profile : approvedProfiles) {
            String province = extractProvince(profile.getStoreAddress());
            if (!StringUtils.hasText(province)) {
                province = OTHER;
            }
            provinceGrouped.computeIfAbsent(province, key -> new ArrayList<MerchantProfile>()).add(profile);
        }

        List<ProvinceMerchantItemVO> provinceStats = provinceGrouped.entrySet().stream()
            .map(entry -> {
                ProvinceMerchantItemVO item = new ProvinceMerchantItemVO();
                item.setProvince(entry.getKey());
                item.setMerchantCount(entry.getValue().size());
                item.setTopMerchants(entry.getValue().stream()
                    .map(this::resolveMerchantName)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList()));
                return item;
            })
            .sorted((left, right) -> Long.compare(right.getMerchantCount(), left.getMerchantCount()))
            .collect(Collectors.toList());

        AdminMapVO vo = new AdminMapVO();
        vo.setProvinceStats(provinceStats);
        vo.setHighlightedProvinces(provinceStats.stream()
            .map(ProvinceMerchantItemVO::getProvince)
            .filter(item -> !OTHER.equals(item) && CANONICAL_PROVINCES.contains(item))
            .collect(Collectors.toList()));
        return vo;
    }

    private List<ActiveTrendItemVO> buildActiveTrend() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<User> users = userService.list(new LambdaQueryWrapper<User>()
            .ge(User::getCreatedAt, startDateTime)
            .orderByAsc(User::getCreatedAt));
        List<FoodPost> posts = foodPostService.list(new LambdaQueryWrapper<FoodPost>()
            .ge(FoodPost::getCreatedAt, startDateTime)
            .orderByAsc(FoodPost::getCreatedAt));

        Map<LocalDate, Long> userCountByDay = users.stream()
            .filter(item -> item.getCreatedAt() != null)
            .collect(Collectors.groupingBy(item -> item.getCreatedAt().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> postCountByDay = posts.stream()
            .filter(item -> item.getCreatedAt() != null)
            .collect(Collectors.groupingBy(item -> item.getCreatedAt().toLocalDate(), Collectors.counting()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<ActiveTrendItemVO> result = new ArrayList<ActiveTrendItemVO>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            ActiveTrendItemVO item = new ActiveTrendItemVO();
            item.setDate(formatter.format(date));
            item.setNewUsers(userCountByDay.getOrDefault(date, 0L));
            item.setNewPosts(postCountByDay.getOrDefault(date, 0L));
            result.add(item);
        }
        return result;
    }

    private List<CategoryShareItemVO> buildCategoryShare() {
        List<FoodPost> approvedPosts = foodPostService.list(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getCreatedAt)
            .orderByDesc(FoodPost::getId));
        if (approvedPosts.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> postCountByCategory = approvedPosts.stream()
            .filter(item -> item.getCategoryId() != null)
            .collect(Collectors.groupingBy(FoodPost::getCategoryId, Collectors.counting()));
        if (postCountByCategory.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = new ArrayList<Long>(postCountByCategory.keySet());
        Map<Long, String> categoryNameMap = foodCategoryService.listByIds(categoryIds).stream()
            .collect(Collectors.toMap(
                FoodCategory::getId,
                category -> StringUtils.hasText(category.getName()) ? category.getName().trim() : "未分类",
                (left, right) -> left,
                HashMap::new
            ));

        return postCountByCategory.entrySet().stream()
            .map(entry -> {
                CategoryShareItemVO item = new CategoryShareItemVO();
                item.setName(categoryNameMap.getOrDefault(entry.getKey(), "未分类"));
                item.setValue(entry.getValue());
                return item;
            })
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .collect(Collectors.toList());
    }

    private List<MerchantCityItemVO> buildMerchantCities() {
        List<MerchantProfile> approvedProfiles = merchantProfileService.list(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getStatus, 1)
            .orderByDesc(MerchantProfile::getUpdatedAt)
            .orderByDesc(MerchantProfile::getId));
        if (approvedProfiles.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> merchantCountByCity = new HashMap<String, Long>();
        for (MerchantProfile profile : approvedProfiles) {
            String city = extractCity(profile.getStoreAddress());
            if (!StringUtils.hasText(city)) {
                city = OTHER;
            }
            merchantCountByCity.put(city, merchantCountByCity.getOrDefault(city, 0L) + 1L);
        }

        return merchantCountByCity.entrySet().stream()
            .map(entry -> {
                MerchantCityItemVO item = new MerchantCityItemVO();
                item.setCity(entry.getKey());
                item.setMerchantCount(entry.getValue());
                return item;
            })
            .sorted((left, right) -> Long.compare(right.getMerchantCount(), left.getMerchantCount()))
            .limit(12)
            .collect(Collectors.toList());
    }

    private String extractProvince(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        String normalized = normalizeAddress(address);

        for (String province : CANONICAL_PROVINCES) {
            if (normalized.contains(province)) {
                return province;
            }
        }
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (normalized.contains(alias.getKey())) {
                return alias.getValue();
            }
        }
        for (Map.Entry<String, String> cityAlias : CITY_TO_PROVINCE.entrySet()) {
            if (normalized.contains(cityAlias.getKey())) {
                return cityAlias.getValue();
            }
        }

        Matcher matcher = PROVINCE_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return canonicalizeProvince(matcher.group(1));
        }
        return null;
    }

    private String canonicalizeProvince(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        if (CANONICAL_PROVINCES.contains(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        if (trimmed.endsWith("省") || trimmed.endsWith("市")) {
            String base = trimmed.substring(0, trimmed.length() - 1);
            return ALIASES.get(base);
        }
        return ALIASES.get(trimmed);
    }

    private String extractCity(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        String normalized = normalizeAddress(address);
        String province = extractProvince(normalized);
        if (StringUtils.hasText(province) && normalized.startsWith(province)) {
            normalized = normalized.substring(province.length());
        }

        for (String municipality : MUNICIPALITIES) {
            if (address.startsWith(municipality)) {
                return municipality.substring(0, municipality.length() - 1);
            }
        }

        Matcher cityMatcher = CITY_PATTERN.matcher(normalized);
        if (cityMatcher.find()) {
            String city = cityMatcher.group(1);
            if (city.endsWith("市")) {
                return city.substring(0, city.length() - 1);
            }
            return city;
        }
        return null;
    }

    private String normalizeAddress(String address) {
        return address.trim().replaceAll("\\s+", "");
    }

    private String resolveMerchantName(MerchantProfile profile) {
        if (StringUtils.hasText(profile.getMerchantName())) {
            return profile.getMerchantName().trim();
        }
        if (StringUtils.hasText(profile.getStoreName())) {
            return profile.getStoreName().trim();
        }
        return "商家" + profile.getId();
    }

    private void ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "No admin permission");
        }
    }
}
