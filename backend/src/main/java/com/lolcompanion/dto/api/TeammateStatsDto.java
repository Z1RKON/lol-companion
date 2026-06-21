package com.lolcompanion.dto.api;

/** Игрок, с которым часто играли в одной команде (по истории матчей). */
public record TeammateStatsDto(
    String puuid,
    String summonerName,
    Integer profileIconId,
    int gamesTogether) {}
