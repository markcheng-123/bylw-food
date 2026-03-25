package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("strategy")
public class Strategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authorUserId;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private Integer status;

    private Integer sort;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
