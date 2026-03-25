package com.bylw.foodforum.vo.home;

import com.bylw.foodforum.vo.category.CategoryVO;
import com.bylw.foodforum.vo.post.FoodPostCardVO;
import java.util.List;

public class HomePageVO {

    private List<CategoryVO> categories;
    private List<FoodPostCardVO> hotPosts;
    private List<FoodPostCardVO> latestPosts;

    public List<CategoryVO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryVO> categories) {
        this.categories = categories;
    }

    public List<FoodPostCardVO> getHotPosts() {
        return hotPosts;
    }

    public void setHotPosts(List<FoodPostCardVO> hotPosts) {
        this.hotPosts = hotPosts;
    }

    public List<FoodPostCardVO> getLatestPosts() {
        return latestPosts;
    }

    public void setLatestPosts(List<FoodPostCardVO> latestPosts) {
        this.latestPosts = latestPosts;
    }
}
