package com.lolcompanion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LinkRiotAccountRequest(
    @NotBlank(message = "Введите Riot ID в формате Имя#Тег")
    @Size(min = 1, max = 22, message = "Riot ID: Имя#Тег, до 22 символов")
    String riotId,
    @NotBlank(message = "Укажите регион") String region) {}
