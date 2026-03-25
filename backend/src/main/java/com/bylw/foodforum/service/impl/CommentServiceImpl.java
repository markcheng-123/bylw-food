package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.comment.CommentCreateDTO;
import com.bylw.foodforum.entity.Comment;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.CommentMapper;
import com.bylw.foodforum.service.CommentService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.comment.CommentVO;
import com.bylw.foodforum.vo.user.UserCenterCommentVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final FoodPostService foodPostService;
    private final UserService userService;

    public CommentServiceImpl(FoodPostService foodPostService, UserService userService) {
        this.foodPostService = foodPostService;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long postId, CommentCreateDTO createDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录后再评论");
        }

        FoodPost post = foodPostService.getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() == 3)) {
            throw new BusinessException("评论的内容不存在");
        }

        if (createDTO.getParentId() != null) {
            Comment parentComment = getById(createDTO.getParentId());
            if (parentComment == null || !postId.equals(parentComment.getPostId())) {
                throw new BusinessException("回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(currentUserId);
        comment.setParentId(createDTO.getParentId());
        comment.setContent(createDTO.getContent().trim());
        comment.setStatus(1);
        save(comment);

        foodPostService.refreshCommentCount(postId);

        User currentUser = userService.getById(currentUserId);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        commentVO.setAuthorId(currentUserId);
        commentVO.setAuthorNickname(currentUser == null ? "用户" : currentUser.getNickname());
        commentVO.setAuthorAvatar(currentUser == null ? null : currentUser.getAvatar());
        return commentVO;
    }

    @Override
    public List<CommentVO> listCommentsByPostId(Long postId) {
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
            .eq(Comment::getPostId, postId)
            .eq(Comment::getStatus, 1)
            .orderByAsc(Comment::getCreatedAt)
            .orderByAsc(Comment::getId));

        if (comments.isEmpty()) {
            return new ArrayList<CommentVO>();
        }

        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left, HashMap::new));

        Map<Long, CommentVO> allComments = new LinkedHashMap<Long, CommentVO>();
        List<CommentVO> roots = new ArrayList<CommentVO>();
        for (Comment comment : comments) {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            User user = userMap.get(comment.getUserId());
            commentVO.setAuthorId(comment.getUserId());
            commentVO.setAuthorNickname(user == null ? "用户" : user.getNickname());
            commentVO.setAuthorAvatar(user == null ? null : user.getAvatar());
            allComments.put(comment.getId(), commentVO);
        }

        for (Comment comment : comments) {
            CommentVO current = allComments.get(comment.getId());
            if (comment.getParentId() == null) {
                roots.add(current);
                continue;
            }
            CommentVO parent = allComments.get(comment.getParentId());
            if (parent == null) {
                roots.add(current);
            } else {
                parent.getReplies().add(current);
            }
        }
        return roots;
    }

    @Override
    public List<UserCenterCommentVO> listCurrentUserComments() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
            .eq(Comment::getUserId, currentUserId)
            .eq(Comment::getStatus, 1)
            .orderByDesc(Comment::getCreatedAt)
            .orderByDesc(Comment::getId));

        if (comments.isEmpty()) {
            return new ArrayList<UserCenterCommentVO>();
        }

        List<Long> postIds = comments.stream().map(Comment::getPostId).distinct().collect(Collectors.toList());
        Map<Long, FoodPost> postMap = foodPostService.listByIds(postIds)
            .stream()
            .collect(Collectors.toMap(FoodPost::getId, item -> item, (left, right) -> left, HashMap::new));

        return comments.stream().map(comment -> {
            UserCenterCommentVO commentVO = new UserCenterCommentVO();
            FoodPost post = postMap.get(comment.getPostId());
            commentVO.setId(comment.getId());
            commentVO.setPostId(comment.getPostId());
            commentVO.setPostTitle(post == null ? "内容已不存在" : post.getTitle());
            commentVO.setPostCoverImage(post == null ? null : post.getCoverImage());
            commentVO.setContent(comment.getContent());
            commentVO.setParentId(comment.getParentId());
            commentVO.setCreatedAt(comment.getCreatedAt());
            return commentVO;
        }).collect(Collectors.toList());
    }
}
