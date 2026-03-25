package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.dto.user.UserLoginDTO;
import com.bylw.foodforum.dto.user.UserProfileUpdateDTO;
import com.bylw.foodforum.dto.user.UserRegisterDTO;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.vo.user.UserLoginVO;
import com.bylw.foodforum.vo.user.UserProfileVO;

public interface UserService extends IService<User> {

    UserProfileVO register(UserRegisterDTO registerDTO);

    UserLoginVO login(UserLoginDTO loginDTO);

    UserProfileVO getCurrentUserProfile();

    UserProfileVO updateCurrentUserProfile(UserProfileUpdateDTO updateDTO);

    User getCurrentUserEntity();
}
