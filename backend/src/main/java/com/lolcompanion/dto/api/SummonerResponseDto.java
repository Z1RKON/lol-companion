package com.lolcompanion.dto.api;

/**
 * Ответ API: профиль призывателя (контракт для TypeScript {@code SummonerDTO}).
 */
public record SummonerResponseDto(
    Long id,
    String puuid,
    String summonerName,
    Integer summonerLevel,
    Integer profileIconId,
    String tier,
    String rank,
    Integer leaguePoints,
    String winRate,
    String region) {}
