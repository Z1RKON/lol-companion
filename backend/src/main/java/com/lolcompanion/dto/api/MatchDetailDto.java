package com.lolcompanion.dto.api;

import java.util.List;

/** Детальная статистика матча (mobile {@code MatchDetailDTO}). */
public record MatchDetailDto(
    String matchId,
    String gameMode,
    Integer gameDurationMinutes,
    Long gameCreationTimestamp,
    String patchVersion,
    List<MatchParticipantDto> participants) {}
