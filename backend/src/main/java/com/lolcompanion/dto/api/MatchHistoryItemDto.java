package com.lolcompanion.dto.api;

/**
 * Ответ API: краткая статистика матча для списка (контракт для TypeScript {@code MatchDTO}).
 */
public record MatchHistoryItemDto(
    String matchId,
    String gameMode,
    Integer gameDurationMinutes,
    Long gameCreationTimestamp,
    String championName,
    String patchVersion,
    Integer kills,
    Integer deaths,
    Integer assists,
    String kda,
    Integer csScore,
    Long goldEarned,
    Boolean win,
    java.util.List<Integer> items) {}
