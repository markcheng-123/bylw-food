package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.category.CategorySaveDTO;
import com.bylw.foodforum.entity.FoodCategory;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.mapper.FoodCategoryMapper;
import com.bylw.foodforum.mapper.FoodPostMapper;
import com.bylw.foodforum.service.FoodCategoryService;
import com.bylw.foodforum.vo.category.CategoryVO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FoodCategoryServiceImpl extends ServiceImpl<FoodCategoryMapper, FoodCategory> implements FoodCategoryService {
    private final FoodPostMapper foodPostMapper;

    public FoodCategoryServiceImpl(FoodPostMapper foodPostMapper) {
        this.foodPostMapper = foodPostMapper;
    }

    @Override
    public List<CategoryVO> listEnabledCategories() {
        return list(new LambdaQueryWrapper<FoodCategory>()
                .eq(FoodCategory::getStatus, 1)
                .orderByAsc(FoodCategory::getSort)
                .orderByDesc(FoodCategory::getId))
            .stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listAllCategoriesForAdmin() {
        return list(new LambdaQueryWrapper<FoodCategory>()
                .orderByAsc(FoodCategory::getSort)
                .orderByDesc(FoodCategory::getId))
            .stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    @Override
    public CategoryVO createCategory(CategorySaveDTO saveDTO) {
        validateName(saveDTO.getName(), null);
        FoodCategory category = new FoodCategory();
        fillCategory(category, saveDTO);
        save(category);
        return toVO(category);
    }

    @Override
    public CategoryVO updateCategory(Long id, CategorySaveDTO saveDTO) {
        FoodCategory category = getExistingCategory(id);
        validateName(saveDTO.getName(), id);
        fillCategory(category, saveDTO);
        updateById(category);
        return toVO(category);
    }

    @Override
    public CategoryVO updateCategoryStatus(Long id, Integer status) {
        FoodCategory category = getExistingCategory(id);
        category.setStatus(status);
        updateById(category);
        return toVO(category);
    }

    @Override
    public void deleteCategory(Long id) {
        FoodCategory category = getExistingCategory(id);
        Long postCount = foodPostMapper.selectCount(new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getCategoryId, id));
        if (postCount != null && postCount > 0) {
            throw new BusinessException("该分类下已有内容，无法删除");
        }
        removeById(category.getId());
    }

    private void fillCategory(FoodCategory category, CategorySaveDTO saveDTO) {
        category.setName(saveDTO.getName().trim());
        category.setIcon(StringUtils.hasText(saveDTO.getIcon()) ? saveDTO.getIcon().trim() : null);
        category.setSort(saveDTO.getSort());
        category.setStatus(saveDTO.getStatus());
        category.setDescription(StringUtils.hasText(saveDTO.getDescription()) ? saveDTO.getDescription().trim() : null);
    }

    private void validateName(String name, Long excludeId) {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<FoodCategory>()
            .eq(FoodCategory::getName, name.trim());
        if (excludeId != null) {
            wrapper.ne(FoodCategory::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    private FoodCategory getExistingCategory(Long id) {
        FoodCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    private CategoryVO toVO(FoodCategory category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }
}
