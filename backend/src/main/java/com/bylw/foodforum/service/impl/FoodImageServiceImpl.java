package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.entity.FoodImage;
import com.bylw.foodforum.mapper.FoodImageMapper;
import com.bylw.foodforum.service.FoodImageService;
import org.springframework.stereotype.Service;

@Service
public class FoodImageServiceImpl extends ServiceImpl<FoodImageMapper, FoodImage> implements FoodImageService {
}
