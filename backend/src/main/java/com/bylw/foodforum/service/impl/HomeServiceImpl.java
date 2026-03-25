package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.mapper.FoodPostMapper;
import com.bylw.foodforum.service.HomeService;
import com.bylw.foodforum.vo.home.HomeStatsVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class HomeServiceImpl implements HomeService {

    private final FoodPostMapper foodPostMapper;

    public HomeServiceImpl(FoodPostMapper foodPostMapper) {
        this.foodPostMapper = foodPostMapper;
    }

    @Override
    public HomeStatsVO getHomeStats() {
        Long totalPosts = foodPostMapper.selectCount(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1));

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Long todayNewPosts = foodPostMapper.selectCount(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .ge(FoodPost::getCreatedAt, start)
            .lt(FoodPost::getCreatedAt, end));

        HomeStatsVO vo = new HomeStatsVO();
        vo.setTotalPosts(totalPosts == null ? 0L : totalPosts);
        vo.setTodayNewPosts(todayNewPosts == null ? 0L : todayNewPosts);
        return vo;
    }
}
