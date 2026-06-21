package com.lolcompanion.controller;

import com.lolcompanion.dto.api.FavoriteSummonerDto;
import com.lolcompanion.dto.api.MatchDetailDto;
import com.lolcompanion.dto.api.MatchHistoryItemDto;
import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.dto.api.TeammateStatsDto;
import com.lolcompanion.dto.request.AddFavoriteRequest;
import com.lolcompanion.security.SecurityUtils;
import com.lolcompanion.service.FavoriteService;
import com.lolcompanion.service.MatchService;
import com.lolcompanion.service.SummonerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Control (PCMEF): призыватели, матчи, избранное. JWT обязателен (кроме /auth/*).
 */
@Slf4j
@RestController
@RequestMapping("/summoner")
@Validated
public class SummonerController {

  private final SummonerService summonerService;
  private final MatchService matchService;
  private final FavoriteService favoriteService;

  public SummonerController(
      SummonerService summonerService,
      MatchService matchService,
      FavoriteService favoriteService) {
    this.summonerService = summonerService;
    this.matchService = matchService;
    this.favoriteService = favoriteService;
  }

  /** GET /api/summoner/search?name=Summoner#TAG&region=RU */
  @GetMapping("/search")
  public ResponseEntity<SummonerResponseDto> searchSummoner(
      @RequestParam("name")
          @NotBlank(message = "Введите игровое имя призывателя")
          @Size(min = 1, max = 22, message = "Riot ID: Имя#Тег, до 22 символов")
          String name,
      @RequestParam(defaultValue = "RU") String region) {
    return ResponseEntity.ok(summonerService.getSummonerProfileByName(name, region));
  }

  /** GET /api/summoner/by-name/{name}?region=RU */
  @GetMapping("/by-name/{name}")
  public ResponseEntity<SummonerResponseDto> getByName(
      @PathVariable
          @NotBlank
          @Size(min = 1, max = 22, message = "Riot ID: Имя#Тег, до 22 символов")
          String name,
      @RequestParam(defaultValue = "RU") String region) {
    return ResponseEntity.ok(summonerService.getSummonerProfileByName(name, region));
  }

  /** GET /api/summoner/matches/{matchId} */
  @GetMapping("/matches/{matchId}")
  public ResponseEntity<MatchDetailDto> getMatchDetail(
      @PathVariable @NotBlank String matchId) {
    return ResponseEntity.ok(matchService.getMatchDetail(matchId));
  }

  /** GET /api/summoner/{puuid} — профиль по PUUID (из локальной БД) */
  @GetMapping("/{puuid}")
  public ResponseEntity<SummonerResponseDto> getByPuuid(
      @PathVariable @NotBlank String puuid) {
    return ResponseEntity.ok(summonerService.getSummonerProfileByPuuid(puuid));
  }

  /** GET /api/summoner/{puuid}/matches */
  @GetMapping("/{puuid}/matches")
  public ResponseEntity<List<MatchHistoryItemDto>> getMatchHistory(
      @PathVariable @NotBlank String puuid,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int count) {
    return ResponseEntity.ok(matchService.getMatchHistory(puuid, count));
  }

  /** GET /api/summoner/{puuid}/teammates — союзники по недавним матчам */
  @GetMapping("/{puuid}/teammates")
  public ResponseEntity<List<TeammateStatsDto>> getTeammates(
      @PathVariable @NotBlank String puuid,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int matches,
      @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
    return ResponseEntity.ok(matchService.getTeammatesFromMatches(puuid, matches, limit));
  }

  /** POST /api/summoner/{puuid}/refresh */
  @PostMapping("/{puuid}/refresh")
  public ResponseEntity<Map<String, Object>> refreshSummoner(@PathVariable @NotBlank String puuid) {
    SummonerResponseDto dto = summonerService.forceRefreshSummonerDto(puuid);
    return ResponseEntity.ok(
        Map.of(
            "summonerName", dto.summonerName(),
            "summonerLevel", dto.summonerLevel(),
            "message", "Информация обновлена"));
  }

  /** GET /api/summoner/favorites */
  @GetMapping("/favorites")
  public ResponseEntity<List<FavoriteSummonerDto>> listFavorites() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(favoriteService.listFavorites(userId));
  }

  /** POST /api/summoner/favorites */
  @PostMapping("/favorites")
  public ResponseEntity<FavoriteSummonerDto> addFavorite(
      @Valid @RequestBody AddFavoriteRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    FavoriteSummonerDto dto = favoriteService.addFavorite(userId, request.puuid());
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  /** DELETE /api/summoner/favorites/{summonerId} */
  @DeleteMapping("/favorites/{summonerId}")
  public ResponseEntity<Void> removeFavorite(@PathVariable Long summonerId) {
    Long userId = SecurityUtils.getCurrentUserId();
    favoriteService.removeFavorite(userId, summonerId);
    return ResponseEntity.noContent().build();
  }
}
