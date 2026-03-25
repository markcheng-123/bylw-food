package com.bylw.foodforum.vo.strategy;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StrategyCardVO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String authorNickname;
    private Integer sort;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
