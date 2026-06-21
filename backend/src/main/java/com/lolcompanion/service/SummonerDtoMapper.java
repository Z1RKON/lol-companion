package com.lolcompanion.service;

import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.entity.Summoner;
import lombok.experimental.UtilityClass;

/** Маппинг Entity → API DTO (Mediator). */
@UtilityClass
class SummonerDtoMapper {

  static SummonerResponseDto toDto(Summoner summoner) {
    return new SummonerResponseDto(
        summoner.getId(),
        summoner.getPuuid(),
        summoner.getSummonerName(),
        summoner.getSummonerLevel(),
        summoner.getProfileIconId(),
        summoner.getTier() != null ? summoner.getTier().name() : "UNRANKED",
        summoner.getRank() != null ? summoner.getRank().name() : null,
        summoner.getLeaguePoints(),
        String.format("%.1f%%", summoner.getWinRate()),
        summoner.getRegion());
  }
}
