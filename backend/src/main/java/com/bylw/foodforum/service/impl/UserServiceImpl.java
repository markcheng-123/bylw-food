package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.common.util.AuthTokenUtil;
import com.bylw.foodforum.dto.user.UserLoginDTO;
import com.bylw.foodforum.dto.user.UserProfileUpdateDTO;
import com.bylw.foodforum.dto.user.UserRegisterDTO;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.UserMapper;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.user.UserLoginVO;
import com.bylw.foodforum.vo.user.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthTokenUtil authTokenUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, AuthTokenUtil authTokenUtil) {
        this.passwordEncoder = passwordEncoder;
        this.authTokenUtil = authTokenUtil;
    }

    @Override
    public UserProfileVO register(UserRegisterDTO registerDTO) {
        String username = registerDTO.getUsername().trim();
        User existedUser = getByUsername(username);
        if (existedUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname().trim());
        user.setPhone(emptyToNull(registerDTO.getPhone()));
        user.setEmail(emptyToNull(registerDTO.getEmail()));
        user.setRole(1);
        user.setStatus(1);
        save(user);
        return toProfileVO(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        User user = getByUsername(loginDTO.getUsername());
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("当前账号不可用，请联系管理员");
        }

        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(authTokenUtil.createToken(user.getId()));
        loginVO.setUserInfo(toProfileVO(user));
        return loginVO;
    }

    @Override
    public UserProfileVO getCurrentUserProfile() {
        return toProfileVO(getCurrentUserEntity());
    }

    @Override
    public UserProfileVO updateCurrentUserProfile(UserProfileUpdateDTO updateDTO) {
        User currentUser = getCurrentUserEntity();
        currentUser.setNickname(updateDTO.getNickname().trim());
        currentUser.setAvatar(emptyToNull(updateDTO.getAvatar()));
        currentUser.setPhone(emptyToNull(updateDTO.getPhone()));
        currentUser.setEmail(emptyToNull(updateDTO.getEmail()));
        updateById(currentUser);
        return toProfileVO(currentUser);
    }

    @Override
    public User getCurrentUserEntity() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(401, "当前用户不存在");
        }
        return user;
    }

    private User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username), false);
    }

    private UserProfileVO toProfileVO(User user) {
        UserProfileVO profileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, profileVO);
        return profileVO;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
