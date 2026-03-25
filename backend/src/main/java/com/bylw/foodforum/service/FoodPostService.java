package com.bylw.foodforum.service;

import com.bylw.foodforum.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.dto.post.FoodPostCreateDTO;
import com.bylw.foodforum.dto.post.FoodPostQueryDTO;
import com.bylw.foodforum.dto.post.FoodPostUpdateDTO;
import com.bylw.foodforum.dto.admin.PostAuditDTO;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.vo.admin.AdminPostVO;
import com.bylw.foodforum.vo.home.HomePageVO;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import com.bylw.foodforum.vo.post.FoodPostDetailVO;
import com.bylw.foodforum.vo.post.FoodPostVO;
import java.util.List;

public interface FoodPostService extends IService<FoodPost> {

    FoodPostVO createPost(FoodPostCreateDTO createDTO);

    FoodPostVO updatePost(Long postId, FoodPostUpdateDTO updateDTO);

    HomePageVO getHomePageData();

    PageResult<FoodPostCardVO> queryPosts(FoodPostQueryDTO queryDTO);

    FoodPostDetailVO getPostDetail(Long id);

    List<FoodPostCardVO> listLatestPosts(int limit);

    List<FoodPostCardVO> listPostsByAuthor(Long userId);

    List<FoodPostCardVO> listPostsByIds(List<Long> postIds);

    int refreshCommentCount(Long postId);

    int changeLikeCount(Long postId, int delta);

    PageResult<AdminPostVO> queryPostsForAdmin(long current, long size, Integer status);

    void auditPost(Long id, PostAuditDTO auditDTO);
}
