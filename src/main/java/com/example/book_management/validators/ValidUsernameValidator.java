package com.example.book_management.validators;

import com.example.book_management.services.UserService;
import com.example.book_management.validators.annotations.ValidUsername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidUsernameValidator implements
        ConstraintValidator<ValidUsername, String> {
    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (userService == null) {
            return true;
        }
        return userService.findByUsername(username).isEmpty();
    }
}