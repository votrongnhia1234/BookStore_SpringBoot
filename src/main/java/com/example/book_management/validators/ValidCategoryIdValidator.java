package com.example.book_management.validators;

import com.example.book_management.models.Category;
import com.example.book_management.validators.annotations.ValidCategoryId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCategoryIdValidator implements ConstraintValidator<ValidCategoryId, Category> {

    @Override
    public boolean isValid(Category category,
            ConstraintValidatorContext context) {
        return category != null && category.getId() != null;
    }
}