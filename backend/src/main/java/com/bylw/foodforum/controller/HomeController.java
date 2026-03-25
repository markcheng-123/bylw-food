package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.vo.home.HomePageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final FoodPostService foodPostService;

    public HomeController(FoodPostService foodPostService) {
        this.foodPostService = foodPostService;
    }

    @GetMapping
    public ApiResponse<HomePageVO> getHomePageData() {
        return ApiResponse.success(foodPostService.getHomePageData());
    }
}
