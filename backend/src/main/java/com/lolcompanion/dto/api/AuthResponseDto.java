package com.lolcompanion.dto.api;

public record AuthResponseDto(
    String accessToken, String tokenType, long expiresInMs, UserResponseDto user) {}
