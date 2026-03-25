package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("food_dish")
public class FoodDish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String dishName;

    private String ingredients;

    private String imageUrl;

    private String allergens;

    private Integer sort;

    private LocalDateTime createdAt;
}
