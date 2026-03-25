package com.bylw.foodforum.vo.user;

public class UserLoginVO {

    private String token;
    private UserProfileVO userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserProfileVO userInfo) {
        this.userInfo = userInfo;
    }
}
