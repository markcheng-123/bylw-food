package com.bylw.foodforum.vo.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private List<CommentVO> replies = new ArrayList<CommentVO>();
}
