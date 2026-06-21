package com.lolcompanion.dto.api;

import java.time.LocalDateTime;

public record FavoriteSummonerDto(
    Long favoriteId,
    Long summonerId,
    String puuid,
    String summonerName,
    Integer summonerLevel,
    String tier,
    String rank,
    Integer leaguePoints,
    String winRate,
    LocalDateTime addedAt) {}
