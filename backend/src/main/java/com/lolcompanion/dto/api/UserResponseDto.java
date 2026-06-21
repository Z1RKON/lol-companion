package com.lolcompanion.dto.api;

public record UserResponseDto(
    Long id,
    String username,
    String email,
    String role,
    String linkedRiotId,
    String linkedRiotRegion,
    String linkedRiotPuuid) {}
