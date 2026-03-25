package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("food_post")
public class FoodPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authorUserId;

    private Long categoryId;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private String address;

    private Integer perCapita;

    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
