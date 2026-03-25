package com.bylw.foodforum.vo.user;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserCenterCommentVO {

    private Long id;
    private Long postId;
    private String postTitle;
    private String postCoverImage;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
}
