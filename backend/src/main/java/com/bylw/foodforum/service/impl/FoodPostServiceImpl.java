package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.admin.PostAuditDTO;
import com.bylw.foodforum.dto.post.FoodPostCreateDTO;
import com.bylw.foodforum.dto.post.FoodPostQueryDTO;
import com.bylw.foodforum.dto.post.FoodPostUpdateDTO;
import com.bylw.foodforum.dto.post.PostDishDTO;
import com.bylw.foodforum.entity.Comment;
import com.bylw.foodforum.entity.FoodCategory;
import com.bylw.foodforum.entity.FoodDish;
import com.bylw.foodforum.entity.FoodImage;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.entity.LikeRecord;
import com.bylw.foodforum.entity.MerchantProfile;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.CommentMapper;
import com.bylw.foodforum.mapper.FoodPostMapper;
import com.bylw.foodforum.mapper.LikeRecordMapper;
import com.bylw.foodforum.service.AuditRecordService;
import com.bylw.foodforum.service.FoodCategoryService;
import com.bylw.foodforum.service.FoodDishService;
import com.bylw.foodforum.service.FoodImageService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.MerchantProfileService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.admin.AdminPostVO;
import com.bylw.foodforum.vo.home.HomePageVO;
import com.bylw.foodforum.vo.post.FoodDishVO;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import com.bylw.foodforum.vo.post.FoodPostDetailVO;
import com.bylw.foodforum.vo.post.FoodPostVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FoodPostServiceImpl extends ServiceImpl<FoodPostMapper, FoodPost> implements FoodPostService {

    private final FoodCategoryService foodCategoryService;
    private final FoodDishService foodDishService;
    private final FoodImageService foodImageService;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final AuditRecordService auditRecordService;
    private final MerchantProfileService merchantProfileService;

    public FoodPostServiceImpl(
        FoodCategoryService foodCategoryService,
        FoodDishService foodDishService,
        FoodImageService foodImageService,
        UserService userService,
        CommentMapper commentMapper,
        LikeRecordMapper likeRecordMapper,
        AuditRecordService auditRecordService,
        MerchantProfileService merchantProfileService
    ) {
        this.foodCategoryService = foodCategoryService;
        this.foodDishService = foodDishService;
        this.foodImageService = foodImageService;
        this.userService = userService;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.auditRecordService = auditRecordService;
        this.merchantProfileService = merchantProfileService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodPostVO createPost(FoodPostCreateDTO createDTO) {
        User currentUser = userService.getCurrentUserEntity();
        FoodCategory category = foodCategoryService.getById(createDTO.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("Selected category is unavailable");
        }
        if (currentUser.getRole() != null && currentUser.getRole() == 2
            && (createDTO.getDishes() == null || createDTO.getDishes().isEmpty())) {
            throw new BusinessException("Merchant must provide at least one dish with ingredients");
        }
        if (currentUser.getRole() != null && currentUser.getRole() == 2 && createDTO.getDishes() != null) {
            for (PostDishDTO dishDTO : createDTO.getDishes()) {
                if (dishDTO.getAllergens() == null || dishDTO.getAllergens().isEmpty()) {
                    throw new BusinessException("Please set at least one allergen tag, or choose none");
                }
                if (dishDTO.getImageUrls() == null || dishDTO.getImageUrls().isEmpty()) {
                    throw new BusinessException("Each dish requires at least one image");
                }
            }
        }

        FoodPost foodPost = new FoodPost();
        foodPost.setAuthorUserId(currentUser.getId());
        foodPost.setCategoryId(createDTO.getCategoryId());
        foodPost.setTitle(createDTO.getTitle().trim());
        foodPost.setSummary(StringUtils.hasText(createDTO.getSummary()) ? createDTO.getSummary().trim() : null);
        foodPost.setContent(createDTO.getContent().trim());
        foodPost.setAddress(StringUtils.hasText(createDTO.getAddress()) ? createDTO.getAddress().trim() : null);
        foodPost.setPerCapita(createDTO.getPerCapita());
        foodPost.setCoverImage(createDTO.getImageUrls().get(0));
        foodPost.setStatus(0);
        foodPost.setViewCount(0);
        foodPost.setLikeCount(0);
        foodPost.setCommentCount(0);
        save(foodPost);

        List<String> imageUrls = new ArrayList<String>(createDTO.getImageUrls());
        for (int i = 0; i < imageUrls.size(); i++) {
            FoodImage foodImage = new FoodImage();
            foodImage.setPostId(foodPost.getId());
            foodImage.setImageUrl(imageUrls.get(i));
            foodImage.setSort(i);
            foodImageService.save(foodImage);
        }
        saveDishes(foodPost.getId(), createDTO.getDishes());

        FoodPostVO foodPostVO = new FoodPostVO();
        BeanUtils.copyProperties(foodPost, foodPostVO);
        foodPostVO.setImageUrls(imageUrls);
        return foodPostVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodPostVO updatePost(Long postId, FoodPostUpdateDTO updateDTO) {
        User currentUser = userService.getCurrentUserEntity();
        FoodPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        if (post.getAuthorUserId() == null || !post.getAuthorUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only author can edit this post");
        }

        FoodCategory category = foodCategoryService.getById(updateDTO.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("Selected category is unavailable");
        }

        post.setCategoryId(updateDTO.getCategoryId());
        post.setTitle(updateDTO.getTitle().trim());
        post.setSummary(StringUtils.hasText(updateDTO.getSummary()) ? updateDTO.getSummary().trim() : null);
        post.setContent(updateDTO.getContent().trim());
        post.setAddress(StringUtils.hasText(updateDTO.getAddress()) ? updateDTO.getAddress().trim() : null);
        post.setPerCapita(updateDTO.getPerCapita());
        updateById(post);

        List<String> imageUrls = foodImageService.list(new LambdaQueryWrapper<FoodImage>()
                .eq(FoodImage::getPostId, postId)
                .orderByAsc(FoodImage::getSort)
                .orderByAsc(FoodImage::getId))
            .stream()
            .map(FoodImage::getImageUrl)
            .collect(Collectors.toList());
        FoodPostVO vo = new FoodPostVO();
        BeanUtils.copyProperties(post, vo);
        vo.setImageUrls(imageUrls);
        return vo;
    }

    @Override
    public HomePageVO getHomePageData() {
        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setCategories(foodCategoryService.listEnabledCategories());
        homePageVO.setHotPosts(toCardVOList(listHotPosts(4)));
        homePageVO.setLatestPosts(listLatestPosts(6));
        return homePageVO;
    }

    @Override
    public PageResult<FoodPostCardVO> queryPosts(FoodPostQueryDTO queryDTO) {
        LambdaQueryWrapper<FoodPost> wrapper = buildVisiblePostWrapper(queryDTO.getCategoryId(), queryDTO.getKeyword());
        long total = count(wrapper);

        long offset = (queryDTO.getCurrent() - 1) * queryDTO.getSize();
        List<FoodPost> posts = list(wrapper.last(
            "ORDER BY (IFNULL(view_count,0) + IFNULL(like_count,0) + IFNULL(comment_count,0)) DESC, created_at DESC LIMIT "
                + offset + "," + queryDTO.getSize()));
        return new PageResult<FoodPostCardVO>(total, toCardVOList(posts));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodPostDetailVO getPostDetail(Long id) {
        FoodPost foodPost = getById(id);
        if (foodPost == null) {
            throw new BusinessException("Post not found");
        }
        Long currentUserId = UserContext.getCurrentUserId();
        User currentUser = currentUserId == null ? null : userService.getById(currentUserId);
        if (!canCurrentUserViewPost(foodPost, currentUser)) {
            throw new BusinessException("Current post is under review or removed");
        }

        foodPost.setViewCount((foodPost.getViewCount() == null ? 0 : foodPost.getViewCount()) + 1);
        updateById(foodPost);

        FoodCategory category = foodCategoryService.getById(foodPost.getCategoryId());
        User author = userService.getById(foodPost.getAuthorUserId());
        List<String> imageUrls = foodImageService.list(new LambdaQueryWrapper<FoodImage>()
                .eq(FoodImage::getPostId, id)
                .orderByAsc(FoodImage::getSort)
                .orderByAsc(FoodImage::getId))
            .stream()
            .map(FoodImage::getImageUrl)
            .collect(Collectors.toList());
        List<FoodDishVO> dishes = foodDishService.list(new LambdaQueryWrapper<FoodDish>()
                .eq(FoodDish::getPostId, id)
                .orderByAsc(FoodDish::getSort)
                .orderByAsc(FoodDish::getId))
            .stream()
            .map(this::toDishVO)
            .collect(Collectors.toList());

        FoodPostDetailVO detailVO = new FoodPostDetailVO();
        BeanUtils.copyProperties(foodPost, detailVO);
        detailVO.setCategoryName(category == null ? "" : category.getName());
        detailVO.setAuthorUserId(foodPost.getAuthorUserId());
        detailVO.setAuthorNickname(author == null ? "匿名用户" : author.getNickname());
        detailVO.setLikedByCurrentUser(hasCurrentUserLikedPost(id));
        detailVO.setImageUrls(imageUrls);
        detailVO.setDishes(dishes);
        fillMerchantInfo(detailVO, foodPost.getAuthorUserId(), author);
        return detailVO;
    }

    @Override
    public List<FoodPostCardVO> listLatestPosts(int limit) {
        List<FoodPost> posts = list(buildVisiblePostWrapper(null, null)
            .orderByDesc(FoodPost::getCreatedAt)
            .last("LIMIT " + limit));
        return toCardVOList(posts);
    }

    @Override
    public List<FoodPostCardVO> listPostsByAuthor(Long userId) {
        if (userId == null) {
            return new ArrayList<FoodPostCardVO>();
        }
        List<FoodPost> posts = list(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getAuthorUserId, userId)
            .orderByDesc(FoodPost::getCreatedAt)
            .orderByDesc(FoodPost::getId));
        return toCardVOList(posts);
    }

    @Override
    public List<FoodPostCardVO> listPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new ArrayList<FoodPostCardVO>();
        }

        Map<Long, Integer> orderMap = new HashMap<Long, Integer>();
        for (int i = 0; i < postIds.size(); i++) {
            orderMap.put(postIds.get(i), i);
        }

        List<FoodPost> posts = list(new LambdaQueryWrapper<FoodPost>().in(FoodPost::getId, postIds));
        List<FoodPostCardVO> cards = toCardVOList(posts);
        cards.sort((left, right) -> Integer.compare(
            orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE),
            orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)));
        return cards;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshCommentCount(Long postId) {
        FoodPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        int nextCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
            .eq(Comment::getPostId, postId)
            .eq(Comment::getStatus, 1)).intValue();
        post.setCommentCount(nextCount);
        updateById(post);
        return nextCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeLikeCount(Long postId, int delta) {
        FoodPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        int current = post.getLikeCount() == null ? 0 : post.getLikeCount();
        int next = current + delta;
        if (next < 0) {
            next = 0;
        }
        post.setLikeCount(next);
        updateById(post);
        return next;
    }

    @Override
    public PageResult<AdminPostVO> queryPostsForAdmin(long current, long size, Integer status) {
        ensureAdmin();
        LambdaQueryWrapper<FoodPost> wrapper = new LambdaQueryWrapper<FoodPost>();
        if (status != null) {
            wrapper.eq(FoodPost::getStatus, status);
        }
        long total = count(wrapper);
        long offset = (current - 1) * size;
        List<FoodPost> posts = list(wrapper.orderByDesc(FoodPost::getCreatedAt).last("LIMIT " + offset + "," + size));
        return new PageResult<AdminPostVO>(total, toAdminVOList(posts));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, PostAuditDTO auditDTO) {
        User admin = ensureAdmin();
        FoodPost post = getById(id);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        boolean approved = Integer.valueOf(1).equals(auditDTO.getResult());
        post.setStatus(approved ? 1 : 3);
        updateById(post);
        auditRecordService.createRecord(
            "food_post",
            id,
            approved ? 1 : 0,
            StringUtils.hasText(auditDTO.getRemark()) ? auditDTO.getRemark().trim() : (approved ? "审核通过" : "审核拒绝"),
            admin.getId()
        );
    }

    private boolean hasCurrentUserLikedPost(Long postId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        return likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
            .eq(LikeRecord::getPostId, postId)
            .eq(LikeRecord::getUserId, currentUserId)) > 0;
    }

    private List<FoodPost> listHotPosts(int limit) {
        return list(buildVisiblePostWrapper(null, null)
            .last("ORDER BY (IFNULL(view_count,0) + IFNULL(like_count,0) + IFNULL(comment_count,0)) DESC, created_at DESC LIMIT " + limit));
    }

    private LambdaQueryWrapper<FoodPost> buildVisiblePostWrapper(Long categoryId, String keyword) {
        LambdaQueryWrapper<FoodPost> wrapper = new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(FoodPost::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(item -> item.like(FoodPost::getTitle, trimmedKeyword)
                .or()
                .like(FoodPost::getSummary, trimmedKeyword));
        }
        return wrapper;
    }

    private List<FoodPostCardVO> toCardVOList(List<FoodPost> posts) {
        if (posts.isEmpty()) {
            return new ArrayList<FoodPostCardVO>();
        }

        List<Long> categoryIds = posts.stream().map(FoodPost::getCategoryId).distinct().collect(Collectors.toList());
        Map<Long, String> categoryMap = foodCategoryService.listByIds(categoryIds)
            .stream()
            .collect(Collectors.toMap(FoodCategory::getId, FoodCategory::getName, (left, right) -> left, HashMap::new));

        List<Long> authorIds = posts.stream().map(FoodPost::getAuthorUserId).distinct().collect(Collectors.toList());
        Map<Long, MerchantProfile> merchantMap = merchantProfileService.mapApprovedProfilesByUserIds(authorIds);
        Map<Long, User> authorMap = userService.listByIds(authorIds)
            .stream()
            .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left, HashMap::new));

        final Set<Long> likedPostIds = new HashSet<Long>();
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null) {
            List<Long> postIds = posts.stream().map(FoodPost::getId).collect(Collectors.toList());
            likedPostIds.addAll(likeRecordMapper.selectList(new LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getUserId, currentUserId)
                    .in(LikeRecord::getPostId, postIds))
                .stream()
                .map(LikeRecord::getPostId)
                .collect(Collectors.toSet()));
        }

        return posts.stream().map(post -> {
            FoodPostCardVO cardVO = new FoodPostCardVO();
            BeanUtils.copyProperties(post, cardVO);
            cardVO.setCategoryName(categoryMap.getOrDefault(post.getCategoryId(), ""));
            cardVO.setHeatScore(calcHeatScore(post));
            cardVO.setLikedByCurrentUser(likedPostIds.contains(post.getId()));
            fillMerchantInfo(cardVO, merchantMap.get(post.getAuthorUserId()), post.getAuthorUserId(), authorMap.get(post.getAuthorUserId()));
            return cardVO;
        }).collect(Collectors.toList());
    }

    private void fillMerchantInfo(FoodPostDetailVO detailVO, Long authorUserId, User author) {
        Map<Long, MerchantProfile> merchantMap = merchantProfileService.mapApprovedProfilesByUserIds(Collections.singletonList(authorUserId));
        fillMerchantInfo(detailVO, merchantMap.get(authorUserId), authorUserId, author);
    }

    private void fillMerchantInfo(FoodPostDetailVO detailVO, MerchantProfile merchantProfile, Long authorUserId, User author) {
        boolean merchantAuthor = author != null && author.getRole() != null && author.getRole() == 2;
        if (merchantProfile == null && !merchantAuthor) {
            detailVO.setCertifiedMerchant(false);
            detailVO.setMerchantLabel(null);
            detailVO.setMerchantName(null);
            detailVO.setMerchantUserId(null);
            detailVO.setMerchantProfileId(null);
            return;
        }
        detailVO.setCertifiedMerchant(merchantProfile != null);
        detailVO.setMerchantLabel(merchantProfile != null
            ? (StringUtils.hasText(merchantProfile.getMerchantName()) ? merchantProfile.getMerchantName() + "商家直发" : "官方认证")
            : "商家发布");
        detailVO.setMerchantName(merchantProfile != null ? merchantProfile.getMerchantName() : (author == null ? null : author.getNickname()));
        detailVO.setMerchantUserId(authorUserId);
        detailVO.setMerchantProfileId(merchantProfile == null ? null : merchantProfile.getId());
    }

    private void fillMerchantInfo(FoodPostCardVO cardVO, MerchantProfile merchantProfile, Long authorUserId, User author) {
        boolean merchantAuthor = author != null && author.getRole() != null && author.getRole() == 2;
        if (merchantProfile == null && !merchantAuthor) {
            cardVO.setCertifiedMerchant(false);
            cardVO.setMerchantLabel(null);
            cardVO.setMerchantName(null);
            cardVO.setMerchantUserId(null);
            cardVO.setMerchantProfileId(null);
            return;
        }
        cardVO.setCertifiedMerchant(merchantProfile != null);
        cardVO.setMerchantLabel(merchantProfile != null
            ? (StringUtils.hasText(merchantProfile.getMerchantName()) ? merchantProfile.getMerchantName() + "商家直发" : "官方认证")
            : "商家发布");
        cardVO.setMerchantName(merchantProfile != null ? merchantProfile.getMerchantName() : (author == null ? null : author.getNickname()));
        cardVO.setMerchantUserId(authorUserId);
        cardVO.setMerchantProfileId(merchantProfile == null ? null : merchantProfile.getId());
    }

    private int calcHeatScore(FoodPost post) {
        int viewCount = post.getViewCount() == null ? 0 : post.getViewCount();
        int likeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();
        int commentCount = post.getCommentCount() == null ? 0 : post.getCommentCount();
        return viewCount + likeCount + commentCount;
    }

    private void saveDishes(Long postId, List<PostDishDTO> dishList) {
        if (dishList == null || dishList.isEmpty()) {
            return;
        }
        for (int i = 0; i < dishList.size(); i++) {
            PostDishDTO dishDTO = dishList.get(i);
            FoodDish dish = new FoodDish();
            dish.setPostId(postId);
            dish.setDishName(dishDTO.getName().trim());
            dish.setIngredients(joinItems(dishDTO.getIngredients()));
            dish.setImageUrl(joinItems(dishDTO.getImageUrls()));
            dish.setAllergens(joinItems(dishDTO.getAllergens()));
            dish.setSort(i);
            foodDishService.save(dish);
        }
    }

    private FoodDishVO toDishVO(FoodDish dish) {
        FoodDishVO vo = new FoodDishVO();
        vo.setName(dish.getDishName());
        vo.setImageUrls(splitItems(dish.getImageUrl()));
        vo.setIngredients(splitItems(dish.getIngredients()));
        vo.setAllergens(splitItems(dish.getAllergens()));
        return vo;
    }

    private String joinItems(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .collect(Collectors.joining(","));
    }

    private List<String> splitItems(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList());
    }

    private List<AdminPostVO> toAdminVOList(List<FoodPost> posts) {
        if (posts.isEmpty()) {
            return new ArrayList<AdminPostVO>();
        }
        List<Long> categoryIds = posts.stream().map(FoodPost::getCategoryId).distinct().collect(Collectors.toList());
        Map<Long, String> categoryMap = foodCategoryService.listByIds(categoryIds)
            .stream()
            .collect(Collectors.toMap(FoodCategory::getId, FoodCategory::getName, (left, right) -> left, HashMap::new));
        List<Long> authorIds = posts.stream().map(FoodPost::getAuthorUserId).distinct().collect(Collectors.toList());
        Map<Long, String> authorMap = userService.listByIds(authorIds)
            .stream()
            .collect(Collectors.toMap(User::getId, User::getNickname, (left, right) -> left, HashMap::new));

        return posts.stream().map(post -> {
            AdminPostVO vo = new AdminPostVO();
            vo.setId(post.getId());
            vo.setCategoryId(post.getCategoryId());
            vo.setCategoryName(categoryMap.getOrDefault(post.getCategoryId(), ""));
            vo.setTitle(post.getTitle());
            vo.setSummary(post.getSummary());
            vo.setContent(post.getContent());
            vo.setCoverImage(post.getCoverImage());
            vo.setAddress(post.getAddress());
            vo.setPerCapita(post.getPerCapita());
            vo.setStatus(post.getStatus());
            vo.setAuthorNickname(authorMap.getOrDefault(post.getAuthorUserId(), "用户"));
            vo.setCreatedAt(post.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    private boolean canCurrentUserViewPost(FoodPost foodPost, User currentUser) {
        Integer status = foodPost.getStatus();
        if (status == null || status == 1) {
            return true;
        }
        if (currentUser == null) {
            return false;
        }
        if (foodPost.getAuthorUserId() != null && foodPost.getAuthorUserId().equals(currentUser.getId())) {
            return true;
        }
        return isAdmin(currentUser);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole() == 9;
    }

    private User ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "Current user is not admin");
        }
        return currentUser;
    }
}

