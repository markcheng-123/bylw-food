package com.bylw.foodforum.vo.strategy;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StrategyDetailVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String authorNickname;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
