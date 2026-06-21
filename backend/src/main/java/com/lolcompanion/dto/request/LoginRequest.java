package com.lolcompanion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Username обязателен")
        @Size(min = 3, max = 50, message = "Username: от 3 до 50 символов")
        String username,
    @NotBlank(message = "Пароль обязателен") String password) {}
