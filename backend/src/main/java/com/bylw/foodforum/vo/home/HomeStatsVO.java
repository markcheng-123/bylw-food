package com.bylw.foodforum.vo.home;

public class HomeStatsVO {

    private Long totalPosts;
    private Long todayNewPosts;

    public Long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Long getTodayNewPosts() {
        return todayNewPosts;
    }

    public void setTodayNewPosts(Long todayNewPosts) {
        this.todayNewPosts = todayNewPosts;
    }
}
