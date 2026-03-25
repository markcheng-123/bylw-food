package com.bylw.foodforum.vo.admin;

import lombok.Data;

@Data
public class ActiveTrendItemVO {

    private String date;

    private long newUsers;

    private long newPosts;
}
