package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.entity.FoodDish;
import com.bylw.foodforum.mapper.FoodDishMapper;
import com.bylw.foodforum.service.FoodDishService;
import org.springframework.stereotype.Service;

@Service
public class FoodDishServiceImpl extends ServiceImpl<FoodDishMapper, FoodDish> implements FoodDishService {
}

