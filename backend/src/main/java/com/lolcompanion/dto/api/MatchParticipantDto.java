package com.lolcompanion.dto.api;

import java.util.List;

/** Участник матча для экрана детальной статистики (mobile {@code MatchParticipantDTO}). */
public record MatchParticipantDto(
    String puuid,
    String summonerName,
    String championName,
    Integer profileIconId,
    Integer kills,
    Integer deaths,
    Integer assists,
    String kda,
    Integer csScore,
    Long goldEarned,
    Boolean win,
    String team,
    List<Integer> items) {}
