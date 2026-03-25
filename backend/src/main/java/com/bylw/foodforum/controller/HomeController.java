package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.service.HomeService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.vo.home.HomePageVO;
import com.bylw.foodforum.vo.home.HomeStatsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final FoodPostService foodPostService;
    private final HomeService homeService;

    public HomeController(FoodPostService foodPostService, HomeService homeService) {
        this.foodPostService = foodPostService;
        this.homeService = homeService;
    }

    @GetMapping
    public ApiResponse<HomePageVO> getHomePageData() {
        return ApiResponse.success(foodPostService.getHomePageData());
    }

    @GetMapping("/stats")
    public ApiResponse<HomeStatsVO> getHomeStats() {
        return ApiResponse.success(homeService.getHomeStats());
    }
}
