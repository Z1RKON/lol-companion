package com.lolcompanion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddFavoriteRequest(
    @NotBlank(message = "Укажите PUUID призывателя")
        @Size(min = 10, max = 128, message = "Некорректный PUUID")
        String puuid) {}
