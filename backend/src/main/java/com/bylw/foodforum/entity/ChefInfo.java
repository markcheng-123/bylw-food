package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("chef_info")
public class ChefInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantProfileId;

    private String chefName;

    private String title;

    private String avatarUrl;

    private String intro;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
