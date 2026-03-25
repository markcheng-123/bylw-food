package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.dto.comment.CommentCreateDTO;
import com.bylw.foodforum.service.CommentService;
import com.bylw.foodforum.vo.comment.CommentVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentVO> createComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateDTO createDTO) {
        return ApiResponse.success("评论发布成功", commentService.createComment(postId, createDTO));
    }

    @GetMapping
    public ApiResponse<List<CommentVO>> listComments(@PathVariable Long postId) {
        return ApiResponse.success(commentService.listCommentsByPostId(postId));
    }
}
