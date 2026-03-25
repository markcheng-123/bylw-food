package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.dto.post.FoodPostCreateDTO;
import com.bylw.foodforum.dto.post.FoodPostQueryDTO;
import com.bylw.foodforum.dto.post.FoodPostUpdateDTO;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import com.bylw.foodforum.vo.post.FoodPostDetailVO;
import com.bylw.foodforum.vo.post.FoodPostVO;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
public class FoodPostController {

    private final FoodPostService foodPostService;

    public FoodPostController(FoodPostService foodPostService) {
        this.foodPostService = foodPostService;
    }

    @PostMapping
    public ApiResponse<FoodPostVO> createPost(@Valid @RequestBody FoodPostCreateDTO createDTO) {
        return ApiResponse.success("Post created", foodPostService.createPost(createDTO));
    }

    @PutMapping("/{id}")
    public ApiResponse<FoodPostVO> updatePost(@PathVariable Long id, @Valid @RequestBody FoodPostUpdateDTO updateDTO) {
        return ApiResponse.success("Post updated", foodPostService.updatePost(id, updateDTO));
    }

    @GetMapping
    public ApiResponse<PageResult<FoodPostCardVO>> queryPosts(@Valid FoodPostQueryDTO queryDTO) {
        return ApiResponse.success(foodPostService.queryPosts(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodPostDetailVO> getPostDetail(@PathVariable Long id) {
        return ApiResponse.success(foodPostService.getPostDetail(id));
    }
}
