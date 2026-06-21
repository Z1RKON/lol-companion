package com.lolcompanion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username обязателен")
        @Size(min = 3, max = 50, message = "Username: от 3 до 50 символов")
        String username,
    @NotBlank(message = "Email обязателен") @Email(message = "Некорректный email") String email,
    @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 100, message = "Пароль: от 6 до 100 символов")
        String password) {}
