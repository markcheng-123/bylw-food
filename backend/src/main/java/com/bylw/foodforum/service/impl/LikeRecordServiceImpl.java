package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.entity.LikeRecord;
import com.bylw.foodforum.mapper.LikeRecordMapper;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.LikeRecordService;
import com.bylw.foodforum.vo.like.LikeStatusVO;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {

    private final FoodPostService foodPostService;

    public LikeRecordServiceImpl(FoodPostService foodPostService) {
        this.foodPostService = foodPostService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeStatusVO likePost(Long postId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录后再点赞");
        }
        ensurePostExists(postId);

        LikeStatusVO statusVO = new LikeStatusVO();
        if (hasLikedPost(postId, currentUserId)) {
            statusVO.setLiked(true);
            statusVO.setLikeCount(readLikeCount(postId));
            return statusVO;
        }

        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setPostId(postId);
        likeRecord.setUserId(currentUserId);
        save(likeRecord);

        statusVO.setLiked(true);
        statusVO.setLikeCount(foodPostService.changeLikeCount(postId, 1));
        return statusVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeStatusVO unlikePost(Long postId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录后再取消点赞");
        }
        ensurePostExists(postId);

        LikeRecord likeRecord = getOne(new LambdaQueryWrapper<LikeRecord>()
            .eq(LikeRecord::getPostId, postId)
            .eq(LikeRecord::getUserId, currentUserId)
            .last("LIMIT 1"));

        LikeStatusVO statusVO = new LikeStatusVO();
        statusVO.setLiked(false);
        if (likeRecord == null) {
            statusVO.setLikeCount(readLikeCount(postId));
            return statusVO;
        }

        removeById(likeRecord.getId());
        statusVO.setLikeCount(foodPostService.changeLikeCount(postId, -1));
        return statusVO;
    }

    @Override
    public boolean hasLikedPost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
        return count(new LambdaQueryWrapper<LikeRecord>()
            .eq(LikeRecord::getPostId, postId)
            .eq(LikeRecord::getUserId, userId)) > 0;
    }

    @Override
    public List<FoodPostCardVO> listCurrentUserLikedPosts() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        List<LikeRecord> likeRecords = list(new LambdaQueryWrapper<LikeRecord>()
            .eq(LikeRecord::getUserId, currentUserId)
            .orderByDesc(LikeRecord::getCreatedAt)
            .orderByDesc(LikeRecord::getId));

        if (likeRecords.isEmpty()) {
            return new ArrayList<FoodPostCardVO>();
        }

        List<Long> postIds = likeRecords.stream().map(LikeRecord::getPostId).collect(Collectors.toList());
        return foodPostService.listPostsByIds(postIds);
    }

    private void ensurePostExists(Long postId) {
        FoodPost post = foodPostService.getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() == 3)) {
            throw new BusinessException("点赞的内容不存在");
        }
    }

    private int readLikeCount(Long postId) {
        FoodPost post = foodPostService.getById(postId);
        return post == null || post.getLikeCount() == null ? 0 : post.getLikeCount();
    }
}
