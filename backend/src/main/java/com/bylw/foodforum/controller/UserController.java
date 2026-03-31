package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.dto.user.UserLoginDTO;
import com.bylw.foodforum.dto.user.UserPasswordChangeDTO;
import com.bylw.foodforum.dto.user.UserProfileUpdateDTO;
import com.bylw.foodforum.dto.user.UserRegisterDTO;
import com.bylw.foodforum.service.CommentService;
import com.bylw.foodforum.service.FoodPostService;
import com.bylw.foodforum.service.LikeRecordService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import com.bylw.foodforum.vo.user.UserCenterCommentVO;
import com.bylw.foodforum.vo.user.UserLoginVO;
import com.bylw.foodforum.vo.user.UserProfileVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FoodPostService foodPostService;
    private final CommentService commentService;
    private final LikeRecordService likeRecordService;

    public UserController(
        UserService userService,
        FoodPostService foodPostService,
        CommentService commentService,
        LikeRecordService likeRecordService
    ) {
        this.userService = userService;
        this.foodPostService = foodPostService;
        this.commentService = commentService;
        this.likeRecordService = likeRecordService;
    }

    @PostMapping("/register")
    public ApiResponse<UserProfileVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return ApiResponse.success("Register success", userService.register(registerDTO));
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return ApiResponse.success("Login success", userService.login(loginDTO));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> getCurrentUserProfile() {
        return ApiResponse.success(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileVO> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        return ApiResponse.success("Profile updated successfully", userService.updateCurrentUserProfile(updateDTO));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changeCurrentUserPassword(@Valid @RequestBody UserPasswordChangeDTO passwordChangeDTO) {
        userService.changeCurrentUserPassword(passwordChangeDTO);
        return ApiResponse.success("密码修改成功", null);
    }

    @GetMapping("/me/posts")
    public ApiResponse<List<FoodPostCardVO>> listCurrentUserPosts() {
        return ApiResponse.success(foodPostService.listPostsByAuthor(userService.getCurrentUserEntity().getId()));
    }

    @GetMapping("/me/comments")
    public ApiResponse<List<UserCenterCommentVO>> listCurrentUserComments() {
        return ApiResponse.success(commentService.listCurrentUserComments());
    }

    @GetMapping("/me/likes")
    public ApiResponse<List<FoodPostCardVO>> listCurrentUserLikes() {
        return ApiResponse.success(likeRecordService.listCurrentUserLikedPosts());
    }
}
