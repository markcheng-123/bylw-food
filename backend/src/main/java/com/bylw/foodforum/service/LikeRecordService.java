package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.entity.LikeRecord;
import com.bylw.foodforum.vo.like.LikeStatusVO;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import java.util.List;

public interface LikeRecordService extends IService<LikeRecord> {

    LikeStatusVO likePost(Long postId);

    LikeStatusVO unlikePost(Long postId);

    boolean hasLikedPost(Long postId, Long userId);

    List<FoodPostCardVO> listCurrentUserLikedPosts();
}
