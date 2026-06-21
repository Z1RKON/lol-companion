package com.lolcompanion.service;

import com.lolcompanion.config.RiotRegion;
import com.lolcompanion.dto.RiotMatchDto;
import com.lolcompanion.dto.api.MatchDetailDto;
import com.lolcompanion.dto.api.MatchHistoryItemDto;
import com.lolcompanion.dto.api.MatchParticipantDto;
import com.lolcompanion.dto.api.TeammateStatsDto;
import com.lolcompanion.entity.Match;
import com.lolcompanion.entity.ParticipantStats;
import com.lolcompanion.entity.Summoner;
import com.lolcompanion.repository.MatchRepository;
import com.lolcompanion.repository.ParticipantStatsRepository;
import com.lolcompanion.repository.SummonerRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mediator: история матчей.
 *
 * <ol>
 *   <li>ID матчей из Riot: {@code /lol/match/v5/matches/by-puuid/{puuid}/ids}</li>
 *   <li>Для каждого ID: если есть в PostgreSQL — читаем (матч неизменяем)</li>
 *   <li>Если нет — {@code /lol/match/v5/matches/{matchId}} → парсинг → Match + ParticipantStats</li>
 *   <li>Сохранение пачки участников в одной транзакции</li>
 * </ol>
 */
@Slf4j
@Service
public class MatchService {

  private static final int DEFAULT_MATCH_COUNT = 20;
  /** В списке только союзники с числом совместных игр строго больше этого порога. */
  private static final int MIN_GAMES_TOGETHER_EXCLUSIVE = 2;

  private final MatchRepository matchRepository;
  private final ParticipantStatsRepository participantStatsRepository;
  private final SummonerRepository summonerRepository;
  private final RiotApiClient riotApiClient;

  public MatchService(
      MatchRepository matchRepository,
      ParticipantStatsRepository participantStatsRepository,
      SummonerRepository summonerRepository,
      RiotApiClient riotApiClient) {
    this.matchRepository = matchRepository;
    this.participantStatsRepository = participantStatsRepository;
    this.summonerRepository = summonerRepository;
    this.riotApiClient = riotApiClient;
  }

  /**
   * История матчей для клиента (DTO).
   *
   * @param puuid Riot PUUID
   * @param count количество матчей (1–100)
   */
  @Transactional
  public List<MatchHistoryItemDto> getMatchHistory(String puuid, int count) {
    int limit = count > 0 ? Math.min(count, 100) : DEFAULT_MATCH_COUNT;
    log.info("История матчей: puuid={}, limit={}", puuid, limit);

    Summoner summoner =
        summonerRepository
            .findByPuuid(puuid)
            .orElseThrow(() -> new IllegalArgumentException("Призыватель не найден: " + puuid));
    RiotRegion region = RiotRegion.fromCode(summoner.getRegion());
    String platformUrl = region.getPlatformUrl();

    String[] matchIds = riotApiClient.getMatchIds(puuid, 0, limit, platformUrl);
    List<MatchHistoryItemDto> result = new ArrayList<>();

    for (String matchId : matchIds) {
      Match match = ensureMatchLoaded(matchId, platformUrl);
      result.add(toHistoryDto(match, puuid));
    }

    return result;
  }

  /**
   * Частые союзники по недавним матчам (одна команда). Не официальный список друзей Riot.
   *
   * @param puuid PUUID игрока
   * @param matchCount сколько последних матчей анализировать
   * @param limit максимум записей в ответе
   */
  @Transactional
  public List<TeammateStatsDto> getTeammatesFromMatches(String puuid, int matchCount, int limit) {
    int matchesToScan = matchCount > 0 ? Math.min(matchCount, 100) : DEFAULT_MATCH_COUNT;
    int topLimit = limit > 0 ? Math.min(limit, 50) : 20;
    log.info("Союзники: puuid={}, matches={}, limit={}", puuid, matchesToScan, topLimit);

    Summoner summoner =
        summonerRepository
            .findByPuuid(puuid)
            .orElseThrow(() -> new IllegalArgumentException("Призыватель не найден: " + puuid));
    RiotRegion region = RiotRegion.fromCode(summoner.getRegion());
    String platformUrl = region.getPlatformUrl();

    String[] matchIds = riotApiClient.getMatchIds(puuid, 0, matchesToScan, platformUrl);
    Map<String, TeammateAccumulator> byPuuid = new HashMap<>();

    for (String matchId : matchIds) {
      Match match = ensureMatchLoaded(matchId, platformUrl);
      ParticipantStats self =
          match.getParticipantStats().stream()
              .filter(ps -> puuid.equals(ps.getRiotPuuid()))
              .findFirst()
              .orElse(null);
      if (self == null) {
        continue;
      }
      Match.Team myTeam = self.getTeam();
      for (ParticipantStats teammate : match.getParticipantStats()) {
        if (puuid.equals(teammate.getRiotPuuid()) || teammate.getTeam() != myTeam) {
          continue;
        }
        String teammatePuuid = teammate.getRiotPuuid();
        byPuuid.compute(
            teammatePuuid,
            (key, acc) -> {
              if (acc == null) {
                return new TeammateAccumulator(
                    teammatePuuid,
                    resolveParticipantDisplayName(teammate),
                    resolveProfileIconId(teammate),
                    1);
              }
              acc.gamesTogether++;
              return acc;
            });
      }
    }

    return byPuuid.values().stream()
        .filter(acc -> acc.getGamesTogether() > MIN_GAMES_TOGETHER_EXCLUSIVE)
        .sorted(Comparator.comparingInt(TeammateAccumulator::getGamesTogether).reversed())
        .limit(topLimit)
        .map(
            acc ->
                new TeammateStatsDto(
                    acc.getPuuid(),
                    acc.getSummonerName(),
                    acc.getProfileIconId(),
                    acc.getGamesTogether()))
        .toList();
  }

  private static final class TeammateAccumulator {
    private final String puuid;
    private final String summonerName;
    private final Integer profileIconId;
    private int gamesTogether;

    private TeammateAccumulator(
        String puuid, String summonerName, Integer profileIconId, int gamesTogether) {
      this.puuid = puuid;
      this.summonerName = summonerName;
      this.profileIconId = profileIconId;
      this.gamesTogether = gamesTogether;
    }

    private String getPuuid() {
      return puuid;
    }

    private String getSummonerName() {
      return summonerName;
    }

    private Integer getProfileIconId() {
      return profileIconId;
    }

    private int getGamesTogether() {
      return gamesTogether;
    }
  }

  /** Детальная статистика матча для mobile MatchDetailScreen. */
  @Transactional
  public MatchDetailDto getMatchDetail(String matchId) {
    String platformUrl = resolvePlatformUrlForMatch(matchId);
    Match match = ensureMatchLoaded(matchId, platformUrl);

    List<MatchParticipantDto> participants =
        match.getParticipantStats().stream()
            .sorted(Comparator.comparing(ParticipantStats::getTeam).thenComparing(ParticipantStats::getChampionName))
            .map(this::toParticipantDto)
            .toList();

    return new MatchDetailDto(
        match.getMatchId(),
        GameModeLabels.display(match.getGameMode()),
        match.getGameDurationMinutes(),
        match.getGameCreationTimestamp(),
        match.getPatchVersion(),
        participants);
  }

  private MatchParticipantDto toParticipantDto(ParticipantStats ps) {
    String summonerName = resolveParticipantDisplayName(ps);

    return new MatchParticipantDto(
        ps.getRiotPuuid(),
        summonerName,
        ps.getChampionName(),
        resolveProfileIconId(ps),
        ps.getKills(),
        ps.getDeaths(),
        ps.getAssists(),
        String.format("%.1f", ps.calculateKda()),
        ps.getCsScore().intValue(),
        ps.getGoldEarned(),
        ps.isWin(),
        ps.getTeam().name(),
        ps.getItemIds());
  }

  private static String resolveParticipantDisplayName(ParticipantStats ps) {
    if (ps.getSummonerDisplayName() != null && !ps.getSummonerDisplayName().isBlank()) {
      return ps.getSummonerDisplayName();
    }
    if (ps.getSummoner() != null
        && ps.getSummoner().getSummonerName() != null
        && !ps.getSummoner().getSummonerName().isBlank()
        && !"Unknown".equalsIgnoreCase(ps.getSummoner().getSummonerName())) {
      return ps.getSummoner().getSummonerName();
    }
    return "Unknown";
  }

  private static Integer resolveProfileIconId(ParticipantStats ps) {
    if (ps.getProfileIconId() != null) {
      return ps.getProfileIconId();
    }
    if (ps.getSummoner() != null && ps.getSummoner().getProfileIconId() != null) {
      return ps.getSummoner().getProfileIconId();
    }
    return null;
  }

  private Match ensureMatchLoaded(String matchId, String platformUrl) {
    return matchRepository
        .findWithParticipantsByMatchId(matchId)
        .filter(match -> !needsRematchImport(match))
        .orElseGet(() -> loadAndPersistMatch(matchId, platformUrl));
  }

  private static boolean needsRematchImport(Match match) {
    if ("UNKNOWN".equalsIgnoreCase(match.getGameMode())) {
      return true;
    }
    if ("CHERRY".equalsIgnoreCase(match.getGameMode())) {
      return true;
    }
    if (match.getParticipantStats().isEmpty()) {
      return true;
    }
    if (hasMissingItemData(match)) {
      return true;
    }
    if (hasMissingProfileIconData(match)) {
      return true;
    }
    long unknownChampions =
        match.getParticipantStats().stream()
            .filter(ps -> "Unknown".equalsIgnoreCase(ps.getChampionName()))
            .count();
    return unknownChampions > match.getParticipantStats().size() / 2;
  }

  private static boolean hasMissingProfileIconData(Match match) {
    return match.getParticipantStats().stream()
        .anyMatch(ps -> ps.getProfileIconId() == null && ps.getGoldEarned() > 500);
  }

  private static boolean hasMissingItemData(Match match) {
    return match.getParticipantStats().stream()
        .anyMatch(
            ps ->
                ps.getGoldEarned() > 500
                    && ps.getItemIds().stream().allMatch(itemId -> itemId == null || itemId == 0));
  }

  /**
   * Загрузка из Riot API и атомарное сохранение Match + всех ParticipantStats.
   */
  @Transactional
  public Match loadAndPersistMatch(String matchId) {
    return loadAndPersistMatch(matchId, RiotRegion.RU.getPlatformUrl());
  }

  @Transactional
  public Match loadAndPersistMatch(String matchId, String platformUrl) {
    matchRepository
        .findWithParticipantsByMatchId(matchId)
        .filter(MatchService::needsRematchImport)
        .ifPresent(
            stale -> {
              log.info("Перезагрузка устаревших данных матча {}", matchId);
              matchRepository.deleteByMatchId(matchId);
            });

    if (matchRepository.existsByMatchId(matchId)) {
      return matchRepository
          .findWithParticipantsByMatchId(matchId)
          .orElseThrow(
              () -> new IllegalStateException("Матч существует, но не загружен с участниками"));
    }

    RiotMatchDto riotMatch = riotApiClient.getMatchDetails(matchId, platformUrl);
    RiotMatchDto.InfoDto info = riotMatch.getInfo();

    if (info == null || info.getParticipants() == null) {
      throw new IllegalStateException("Пустой ответ Riot API для матча " + matchId);
    }

    int winningTeamId = resolveWinningTeamId(info);
    int durationSeconds = resolveGameDurationSeconds(info);

    Match match =
        Match.fromRiotSummary(
            matchId,
            GameModeLabels.fromRiotInfo(info),
            durationSeconds,
            info.getGameCreation() != null ? info.getGameCreation() : 0L,
            info.getGameStartTimestamp(),
            Match.Team.fromRiotTeamId(winningTeamId),
            info.getPlatformId(),
            info.getGameVersion());

    Match savedMatch = matchRepository.save(match);

    for (RiotMatchDto.ParticipantDto participantDto : info.getParticipants()) {
      ParticipantStats stats = mapParticipant(participantDto, savedMatch);
      savedMatch.addParticipant(stats);
      participantStatsRepository.save(stats);
    }

    log.info(
        "Матч {} сохранён в PostgreSQL ({} участников)",
        matchId,
        savedMatch.getParticipantStats().size());
    return savedMatch;
  }

  private MatchHistoryItemDto toHistoryDto(Match match, String puuid) {
    ParticipantStats participant =
        match.getParticipantStats().stream()
            .filter(ps -> puuid.equals(ps.getRiotPuuid()))
            .findFirst()
            .orElse(null);

    if (participant == null) {
      return new MatchHistoryItemDto(
          match.getMatchId(),
          GameModeLabels.display(match.getGameMode()),
          match.getGameDurationMinutes(),
          match.getGameCreationTimestamp(),
          "Unknown",
          match.getPatchVersion(),
          0,
          0,
          0,
          "0.0",
          0,
          0L,
          false,
          java.util.List.of());
    }

    return new MatchHistoryItemDto(
        match.getMatchId(),
        GameModeLabels.display(match.getGameMode()),
        match.getGameDurationMinutes(),
        match.getGameCreationTimestamp(),
        participant.getChampionName(),
        match.getPatchVersion(),
        participant.getKills(),
        participant.getDeaths(),
        participant.getAssists(),
        String.format("%.1f", participant.calculateKda()),
        participant.getCsScore().intValue(),
        participant.getGoldEarned(),
        participant.isWin(),
        participant.getItemIds());
  }

  private ParticipantStats mapParticipant(RiotMatchDto.ParticipantDto dto, Match match) {
    String puuid = dto.getPuuid();
    String displayName = formatParticipantDisplayName(dto);
    Summoner summoner =
        summonerRepository
            .findByPuuid(puuid)
            .orElseGet(() -> createPlaceholderSummoner(puuid, match.getRegion(), displayName));
    enrichPlaceholderSummoner(summoner, dto, displayName);

    return ParticipantStats.create(
        match,
        summoner,
        puuid,
        displayName,
        dto.getChampionName() != null ? dto.getChampionName() : "Unknown",
        mapRole(dto.getTeamPosition()),
        Match.Team.fromRiotTeamId(dto.getTeamId() != null ? dto.getTeamId() : 100),
        Boolean.TRUE.equals(dto.getWin()),
        nullSafe(dto.getKills()),
        nullSafe(dto.getDeaths()),
        nullSafe(dto.getAssists()),
        resolveCs(dto),
        dto.getGoldEarned() != null ? dto.getGoldEarned() : 0L,
        nullSafeLong(dto.getTotalDamageDealtToChampions()),
        nullSafeLong(dto.getTotalDamageDealtToChampions()),
        dto.getProfileIcon(),
        nullSafe(dto.getItem0()),
        nullSafe(dto.getItem1()),
        nullSafe(dto.getItem2()),
        nullSafe(dto.getItem3()),
        nullSafe(dto.getItem4()),
        nullSafe(dto.getItem5()),
        nullSafe(dto.getItem6()));
  }

  private Summoner createPlaceholderSummoner(String puuid, String region, String displayName) {
    Summoner placeholder = new Summoner();
    placeholder.setPuuid(puuid);
    placeholder.setSummonerName(displayName);
    placeholder.setSummonerLevel(1);
    placeholder.setRegion(region);
    // Не помечаем как свежий профиль — при поиске игрока данные подтянутся из Riot API.
    placeholder.setLastUpdated(java.time.LocalDateTime.now().minusYears(1));
    return summonerRepository.save(placeholder);
  }

  private void enrichPlaceholderSummoner(
      Summoner summoner, RiotMatchDto.ParticipantDto dto, String displayName) {
    if (summoner.getProfileIconId() != null) {
      return;
    }
    if (dto.getProfileIcon() != null) {
      summoner.setProfileIconId(dto.getProfileIcon());
    }
    if (displayName != null
        && !displayName.isBlank()
        && !"Unknown".equalsIgnoreCase(displayName)) {
      summoner.setSummonerName(displayName);
    }
    summonerRepository.save(summoner);
  }

  private static String formatParticipantDisplayName(RiotMatchDto.ParticipantDto dto) {
    String gameName = dto.getRiotIdGameName();
    String tagLine = dto.getRiotIdTagline();
    if (gameName != null && !gameName.isBlank() && tagLine != null && !tagLine.isBlank()) {
      return gameName + "#" + tagLine;
    }
    if (dto.getSummonerName() != null && !dto.getSummonerName().isBlank()) {
      return dto.getSummonerName();
    }
    return "Unknown";
  }

  private static int resolveGameDurationSeconds(RiotMatchDto.InfoDto info) {
    if (info.getGameEndTimestamp() != null
        && info.getGameStartTimestamp() != null
        && info.getGameEndTimestamp() > info.getGameStartTimestamp()) {
      return (int) ((info.getGameEndTimestamp() - info.getGameStartTimestamp()) / 1000);
    }
    if (info.getGameDuration() != null && info.getGameDuration() > 0) {
      return info.getGameDuration();
    }
    return 1;
  }

  private static double resolveCs(RiotMatchDto.ParticipantDto dto) {
    int minions = dto.getTotalMinionsKilled() != null ? dto.getTotalMinionsKilled() : 0;
    int jungle = dto.getNeutralMinionsKilled() != null ? dto.getNeutralMinionsKilled() : 0;
    return minions + jungle;
  }

  private static int nullSafe(Integer value) {
    return value != null ? value : 0;
  }

  private static long nullSafeLong(Long value) {
    return value != null ? value : 0L;
  }

  private static ParticipantStats.Role mapRole(String teamPosition) {
    if (teamPosition == null) {
      return ParticipantStats.Role.UNKNOWN;
    }
    return switch (teamPosition.toUpperCase()) {
      case "TOP" -> ParticipantStats.Role.TOP;
      case "JUNGLE" -> ParticipantStats.Role.JUNGLE;
      case "MIDDLE" -> ParticipantStats.Role.MIDDLE;
      case "BOTTOM" -> ParticipantStats.Role.ADC;
      case "UTILITY" -> ParticipantStats.Role.SUPPORT;
      default -> ParticipantStats.Role.UNKNOWN;
    };
  }

  private static int resolveWinningTeamId(RiotMatchDto.InfoDto info) {
    if (info.getTeams() != null && !info.getTeams().isEmpty()) {
      OptionalInt fromTeams =
          info.getTeams().stream()
              .filter(team -> Boolean.TRUE.equals(team.getWin()))
              .map(RiotMatchDto.TeamDto::getTeamId)
              .filter(Objects::nonNull)
              .mapToInt(Integer::intValue)
              .findFirst();
      if (fromTeams.isPresent()) {
        return fromTeams.getAsInt();
      }
    }

    if (info.getParticipants() != null) {
      return info.getParticipants().stream()
          .filter(participant -> Boolean.TRUE.equals(participant.getWin()))
          .map(RiotMatchDto.ParticipantDto::getTeamId)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(100);
    }

    return 100;
  }

  private String resolvePlatformUrlForMatch(String matchId) {
    return matchRepository
        .findWithParticipantsByMatchId(matchId)
        .flatMap(
            match ->
                match.getParticipantStats().stream()
                    .map(ParticipantStats::getSummoner)
                    .filter(s -> s != null && s.getRegion() != null)
                    .findFirst()
                    .map(s -> RiotRegion.fromCode(s.getRegion()).getPlatformUrl()))
        .orElse(RiotRegion.RU.getPlatformUrl());
  }
}
