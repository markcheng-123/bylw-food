package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.category.CategorySaveDTO;
import com.bylw.foodforum.dto.category.CategoryStatusDTO;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.service.FoodCategoryService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.category.CategoryVO;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final FoodCategoryService foodCategoryService;
    private final UserService userService;

    public CategoryController(FoodCategoryService foodCategoryService, UserService userService) {
        this.foodCategoryService = foodCategoryService;
        this.userService = userService;
    }

    @GetMapping("/options")
    public ApiResponse<List<CategoryVO>> listEnabledCategories() {
        return ApiResponse.success(foodCategoryService.listEnabledCategories());
    }

    @GetMapping("/admin/list")
    public ApiResponse<List<CategoryVO>> listAllCategoriesForAdmin() {
        ensureAdmin();
        return ApiResponse.success(foodCategoryService.listAllCategoriesForAdmin());
    }

    @PostMapping("/admin")
    public ApiResponse<CategoryVO> createCategory(@Valid @RequestBody CategorySaveDTO saveDTO) {
        ensureAdmin();
        return ApiResponse.success("分类创建成功", foodCategoryService.createCategory(saveDTO));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<CategoryVO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO saveDTO) {
        ensureAdmin();
        return ApiResponse.success("分类更新成功", foodCategoryService.updateCategory(id, saveDTO));
    }

    @PutMapping("/admin/{id}/status")
    public ApiResponse<CategoryVO> updateCategoryStatus(@PathVariable Long id, @Valid @RequestBody CategoryStatusDTO statusDTO) {
        ensureAdmin();
        return ApiResponse.success("分类状态已更新", foodCategoryService.updateCategoryStatus(id, statusDTO.getStatus()));
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        ensureAdmin();
        foodCategoryService.deleteCategory(id);
        return ApiResponse.success("分类删除成功", null);
    }

    private void ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "当前账号没有分类管理权限");
        }
    }
}
