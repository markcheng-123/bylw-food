package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.entity.Comment;
import com.bylw.foodforum.dto.comment.CommentCreateDTO;
import com.bylw.foodforum.vo.comment.CommentVO;
import com.bylw.foodforum.vo.user.UserCenterCommentVO;
import java.util.List;

public interface CommentService extends IService<Comment> {

    CommentVO createComment(Long postId, CommentCreateDTO createDTO);

    List<CommentVO> listCommentsByPostId(Long postId);

    List<UserCenterCommentVO> listCurrentUserComments();
}
