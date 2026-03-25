package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.service.LikeRecordService;
import com.bylw.foodforum.vo.like.LikeStatusVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/like")
public class LikeController {

    private final LikeRecordService likeRecordService;

    public LikeController(LikeRecordService likeRecordService) {
        this.likeRecordService = likeRecordService;
    }

    @PostMapping
    public ApiResponse<LikeStatusVO> likePost(@PathVariable Long postId) {
        return ApiResponse.success("点赞成功", likeRecordService.likePost(postId));
    }

    @DeleteMapping
    public ApiResponse<LikeStatusVO> unlikePost(@PathVariable Long postId) {
        return ApiResponse.success("已取消点赞", likeRecordService.unlikePost(postId));
    }
}
