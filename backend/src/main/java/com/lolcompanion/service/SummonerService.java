package com.lolcompanion.service;

import com.lolcompanion.config.RiotApiProperties;
import com.lolcompanion.config.RiotRegion;
import com.lolcompanion.dto.RiotAccountDto;
import com.lolcompanion.dto.RiotLeagueEntryDto;
import com.lolcompanion.dto.RiotSummonerDto;
import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.Summoner.Rank;
import com.lolcompanion.entity.Summoner.Tier;
import com.lolcompanion.exception.RiotApiException;
import com.lolcompanion.exception.SummonerNotFoundException;
import com.lolcompanion.repository.SummonerRepository;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mediator: профиль призывателя.
 *
 * <p>Cache-Aside + TTL ({@code riot.api.cache-ttl-minutes}, по умолчанию 10 мин):
 * <ol>
 *   <li>Поиск в PostgreSQL по имени (без учёта регистра)</li>
 *   <li>Если найден и {@code lastUpdated} свежий — ответ из БД</li>
 *   <li>Иначе — Riot API → маппинг → save → DTO клиенту</li>
 * </ol>
 */
@Slf4j
@Service
@Transactional
public class SummonerService {

  private static final String RANKED_SOLO_QUEUE = "RANKED_SOLO_5x5";

  private final SummonerRepository summonerRepository;
  private final RiotApiClient riotApiClient;
  private final RiotApiProperties riotApiProperties;

  public SummonerService(
      SummonerRepository summonerRepository,
      RiotApiClient riotApiClient,
      RiotApiProperties riotApiProperties) {
    this.summonerRepository = summonerRepository;
    this.riotApiClient = riotApiClient;
    this.riotApiProperties = riotApiProperties;
  }

  /**
   * Публичный метод Mediator: профиль для REST-клиента (DTO).
   *
   * @param summonerName ник призывателя
   * @return {@link SummonerResponseDto}
   */
  public SummonerResponseDto getSummonerProfileByName(String summonerName) {
    return getSummonerProfileByName(summonerName, RiotRegion.RU.getCode());
  }

  public SummonerResponseDto getSummonerProfileByName(String summonerName, String regionCode) {
    return SummonerDtoMapper.toDto(getSummonerByName(summonerName, regionCode));
  }

  public Summoner getSummonerByName(String summonerName) {
    return getSummonerByName(summonerName, RiotRegion.RU.getCode());
  }

  /**
   * Cache-Aside: сущность из БД или обновление из Riot API.
   */
  public Summoner getSummonerByName(String summonerName, String regionCode) {
    String normalized = RiotIdNormalizer.normalizeQuery(summonerName);
    RiotRegion region = RiotRegion.fromCode(regionCode);
    log.info("Запрос профиля: {} (регион {})", normalized, region.getCode());

    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Введите Riot ID в формате Имя#Тег");
    }

    if (normalized.contains("#")) {
      return getSummonerByRiotId(normalized, region);
    }

    Optional<Summoner> cached =
        summonerRepository.findBySummonerNameIgnoreCaseAndRegion(
            normalized, region.getStorageCode());

    int ttlMinutes = riotApiProperties.getCacheTtlMinutes();

    if (cached.isPresent()
        && cached.get().isCacheFresh(ttlMinutes)
        && hasCompleteProfile(cached.get())) {
      Summoner summoner = cached.get();
      if (summoner.getTier() == null) {
        refreshLeagueEntries(summoner, region);
        return summonerRepository.save(summoner);
      }
      log.debug("Кэш актуален (< {} мин), отдаём из PostgreSQL", ttlMinutes);
      return summoner;
    }

    log.debug("Кэш отсутствует или устарел — запрос к Riot API");
    RiotSummonerDto riotData = fetchSummonerByNameFromRiot(normalized, region);

    Summoner summoner = cached.orElseGet(Summoner::new);
    syncSummonerFromRiot(summoner, riotData, region);

    return summonerRepository.save(summoner);
  }

  private Summoner getSummonerByRiotId(String riotId, RiotRegion region) {
    RiotIdNormalizer.ParsedRiotId parsed = RiotIdNormalizer.parseRiotId(riotId);
    String gameName = parsed.gameName();
    String tagLine = parsed.tagLine();

    if (!parsed.hasTag() || gameName.isEmpty()) {
      throw new IllegalArgumentException("Формат Riot ID: Имя#Тег (например Summoner#TAG)");
    }

    if (gameName.length() > 16) {
      throw new IllegalArgumentException("Имя в Riot ID: не более 16 символов");
    }
    if (tagLine.length() > 5) {
      throw new IllegalArgumentException("Тег в Riot ID: не более 5 символов");
    }

    RiotAccountDto account;
    try {
      account = riotApiClient.getAccountByRiotId(gameName, tagLine, region.getPlatformUrl());
    } catch (RiotApiException e) {
      if (e.getHttpStatus() == 404) {
        throw new SummonerNotFoundException(parsed.fullRiotId(), region.getCode());
      }
      throw e;
    }
    int ttlMinutes = riotApiProperties.getCacheTtlMinutes();

    Optional<Summoner> cached = summonerRepository.findByPuuid(account.getPuuid());
    if (cached.isPresent()
        && cached.get().isCacheFresh(ttlMinutes)
        && hasCompleteProfile(cached.get())) {
      Summoner summoner = cached.get();
      if (summoner.getTier() == null) {
        refreshLeagueEntries(summoner, region);
        return summonerRepository.save(summoner);
      }
      log.debug("Кэш по PUUID актуален (< {} мин)", ttlMinutes);
      return summoner;
    }

    log.debug("Профиль отсутствует или неполный — обновление из Riot API");

    RiotSummonerDto riotData;
    try {
      riotData = riotApiClient.getSummonerByPuuid(account.getPuuid(), region.getRegionalUrl());
    } catch (RiotApiException e) {
      if (e.getHttpStatus() == 404) {
        throw new SummonerNotFoundException(riotId, region.getCode());
      }
      throw e;
    }

    Summoner summoner = cached.orElseGet(Summoner::new);
    syncSummonerFromRiot(summoner, riotData, region, formatRiotId(account));
    return summonerRepository.save(summoner);
  }

  @Transactional(readOnly = true)
  public Summoner getSummonerByPuuid(String puuid) {
    return summonerRepository
        .findByPuuid(puuid)
        .orElseThrow(() -> new SummonerNotFoundException("PUUID не найден: " + puuid));
  }

  public SummonerResponseDto getSummonerProfileByPuuid(String puuid) {
    return SummonerDtoMapper.toDto(getSummonerByPuuid(puuid));
  }

  @Transactional
  public Summoner forceRefreshSummoner(String puuid) {
    Summoner summoner =
        summonerRepository
            .findByPuuid(puuid)
            .orElseThrow(() -> new SummonerNotFoundException("PUUID: " + puuid));

    RiotRegion region = RiotRegion.fromCode(summoner.getRegion());
    RiotSummonerDto riotData =
        riotApiClient.getSummonerByPuuid(summoner.getPuuid(), region.getRegionalUrl());
    String displayName = resolveDisplayNameForRefresh(summoner, riotData, region);
    syncSummonerFromRiot(summoner, riotData, region, displayName);
    return summonerRepository.save(summoner);
  }

  public SummonerResponseDto forceRefreshSummonerDto(String puuid) {
    return SummonerDtoMapper.toDto(forceRefreshSummoner(puuid));
  }

  private RiotSummonerDto fetchSummonerByNameFromRiot(String summonerName, RiotRegion region) {
    try {
      return riotApiClient.getSummonerByName(summonerName, region.getRegionalUrl());
    } catch (RiotApiException e) {
      if (e.getHttpStatus() == 404) {
        throw new SummonerNotFoundException(summonerName, region.getCode());
      }
      throw e;
    }
  }

  private void syncSummonerFromRiot(
      Summoner summoner, RiotSummonerDto riotData, RiotRegion region) {
    syncSummonerFromRiot(summoner, riotData, region, null);
  }

  private void syncSummonerFromRiot(
      Summoner summoner,
      RiotSummonerDto riotData,
      RiotRegion region,
      String displayNameFallback) {
    summoner.setPuuid(riotData.getPuuid());
    summoner.updateFromRiot(
        resolveDisplayName(riotData.getSummonerName(), displayNameFallback, summoner.getSummonerName()),
        riotData.getSummonerLevel() != null ? riotData.getSummonerLevel() : 1,
        riotData.getProfileIconId(),
        region.getStorageCode());

    applyLeagueEntries(summoner, fetchLeagueEntries(riotData, region));
  }

  /** League API по PUUID (основной путь); by-summoner — fallback для старых ответов. */
  private RiotLeagueEntryDto[] fetchLeagueEntries(RiotSummonerDto riotData, RiotRegion region) {
    String regionalUrl = region.getRegionalUrl();

    if (riotData.getPuuid() != null && !riotData.getPuuid().isBlank()) {
      RiotLeagueEntryDto[] byPuuid =
          riotApiClient.getLeagueEntriesByPuuid(riotData.getPuuid(), regionalUrl);
      if (byPuuid.length > 0) {
        return byPuuid;
      }
    }

    return riotApiClient.getLeagueEntriesBySummonerId(riotData.getSummonerId(), regionalUrl);
  }

  private void refreshLeagueEntries(Summoner summoner, RiotRegion region) {
    RiotLeagueEntryDto[] entries =
        riotApiClient.getLeagueEntriesByPuuid(summoner.getPuuid(), region.getRegionalUrl());
    applyLeagueEntries(summoner, entries);
  }

  /** Summoner-V4 больше не возвращает поле name — подставляем Riot ID из Account API. */
  private String resolveDisplayNameForRefresh(
      Summoner summoner, RiotSummonerDto riotData, RiotRegion region) {
    if (riotData.getSummonerName() != null && !riotData.getSummonerName().isBlank()) {
      return riotData.getSummonerName();
    }
    if (summoner.getSummonerName() != null && !summoner.getSummonerName().isBlank()) {
      return summoner.getSummonerName();
    }
    RiotAccountDto account =
        riotApiClient.getAccountByPuuid(summoner.getPuuid(), region.getPlatformUrl());
    return formatRiotId(account);
  }

  private static String resolveDisplayName(
      String riotSummonerName, String fallback, String existingName) {
    if (riotSummonerName != null && !riotSummonerName.isBlank()) {
      return riotSummonerName;
    }
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (existingName != null && !existingName.isBlank()) {
      return existingName;
    }
    throw new IllegalArgumentException("Имя призывателя обязательно");
  }

  private static String formatRiotId(RiotAccountDto account) {
    return account.getGameName() + "#" + account.getTagLine();
  }

  /** Заглушки из матчей (level=1, icon=null) не должны блокировать обновление профиля. */
  private static boolean hasCompleteProfile(Summoner summoner) {
    return summoner.getProfileIconId() != null && summoner.getSummonerLevel() > 1;
  }

  private void applyLeagueEntries(Summoner summoner, RiotLeagueEntryDto[] entries) {
    if (entries == null || entries.length == 0) {
      summoner.clearRankedStats();
      return;
    }

    RiotLeagueEntryDto entry =
        Arrays.stream(entries)
            .filter(e -> RANKED_SOLO_QUEUE.equals(e.getQueueType()))
            .findFirst()
            .orElse(entries[0]);

    Tier tier = parseTier(entry.getTier());
    Rank rank = parseRank(entry.getRank());

    if (tier == null) {
      summoner.clearRankedStats();
      return;
    }

    summoner.updateRankedStats(
        tier,
        rank,
        entry.getLeaguePoints() != null ? entry.getLeaguePoints() : 0,
        entry.getWins() != null ? entry.getWins() : 0,
        entry.getLosses() != null ? entry.getLosses() : 0);
  }

  private static Tier parseTier(String tier) {
    if (tier == null || tier.isBlank()) {
      return null;
    }
    try {
      return Tier.valueOf(tier.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static Rank parseRank(String rank) {
    if (rank == null || rank.isBlank()) {
      return null;
    }
    try {
      return Rank.valueOf(rank.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
