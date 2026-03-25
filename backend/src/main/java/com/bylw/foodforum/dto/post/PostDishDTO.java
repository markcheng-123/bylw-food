package com.bylw.foodforum.dto.post;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class PostDishDTO {

    @NotBlank(message = "Dish name is required")
    @Size(max = 64, message = "Dish name must be at most 64 chars")
    private String name;

    @NotEmpty(message = "Please select at least one ingredient")
    private List<@NotBlank(message = "Ingredient cannot be blank") @Size(max = 32, message = "Ingredient too long") String> ingredients;

    @NotEmpty(message = "Please upload at least one dish image")
    private List<@NotBlank(message = "Dish image URL cannot be blank") @Size(max = 255, message = "Dish image URL too long") String> imageUrls;

    private List<@NotBlank(message = "Allergen tag cannot be blank") @Size(max = 32, message = "Allergen tag too long") String> allergens;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }
}
