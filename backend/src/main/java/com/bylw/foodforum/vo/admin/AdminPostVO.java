package com.bylw.foodforum.vo.admin;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminPostVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String address;
    private Integer perCapita;
    private Integer status;
    private String authorNickname;
    private LocalDateTime createdAt;
}
