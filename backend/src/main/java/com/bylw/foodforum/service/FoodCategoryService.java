package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.dto.category.CategorySaveDTO;
import com.bylw.foodforum.entity.FoodCategory;
import com.bylw.foodforum.vo.category.CategoryVO;
import java.util.List;

public interface FoodCategoryService extends IService<FoodCategory> {

    List<CategoryVO> listEnabledCategories();

    List<CategoryVO> listAllCategoriesForAdmin();

    CategoryVO createCategory(CategorySaveDTO saveDTO);

    CategoryVO updateCategory(Long id, CategorySaveDTO saveDTO);

    CategoryVO updateCategoryStatus(Long id, Integer status);

    void deleteCategory(Long id);
}
