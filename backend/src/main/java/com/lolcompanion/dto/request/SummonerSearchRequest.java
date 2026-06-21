package com.lolcompanion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Query-параметр поиска призывателя. */
public record SummonerSearchRequest(
    @NotBlank(message = "Введите игровое имя призывателя")
        @Size(min = 1, max = 20, message = "Ник: от 1 до 20 символов")
        String name) {}
