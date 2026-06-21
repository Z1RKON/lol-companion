package com.lolcompanion.service;

import com.lolcompanion.config.RiotApiProperties;
import com.lolcompanion.dto.RiotAccountDto;
import com.lolcompanion.dto.RiotLeagueEntryDto;
import com.lolcompanion.dto.RiotMatchDto;
import com.lolcompanion.dto.RiotSummonerDto;
import com.lolcompanion.exception.RiotApiException;
import com.lolcompanion.exception.RiotRateLimitException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP-клиент Riot Games API (Mediator). Инкапсулирует все внешние GET-запросы.
 */
@Slf4j
@Component
public class RiotApiClient {

  private final RestTemplate riotRestTemplate;
  private final RiotApiProperties properties;

  public RiotApiClient(
      @Qualifier("riotRestTemplate") RestTemplate riotRestTemplate,
      RiotApiProperties properties) {
    this.riotRestTemplate = riotRestTemplate;
    this.properties = properties;
  }

  /** GET /riot/account/v1/accounts/by-puuid/{puuid} */
  public RiotAccountDto getAccountByPuuid(String puuid, String platformUrl) {
    URI uri =
        buildUri(platformUrl, "/riot/account/v1/accounts/by-puuid/{puuid}", puuid);

    log.debug("Riot API: account by-puuid {}", puuid);

    try {
      RiotAccountDto account = riotRestTemplate.getForObject(uri, RiotAccountDto.class);
      if (account == null || account.getPuuid() == null) {
        throw new RiotApiException(404, "Аккаунт не найден");
      }
      return account;
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }
  }

  /** GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine} */
  public RiotAccountDto getAccountByRiotId(
      String gameName, String tagLine, String platformUrl) {
    URI uri =
        buildUri(
            platformUrl,
            "/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}",
            gameName,
            tagLine);

    log.debug("Riot API: account by-riot-id {}#{}", gameName, tagLine);

    try {
      RiotAccountDto account = riotRestTemplate.getForObject(uri, RiotAccountDto.class);
      if (account == null || account.getPuuid() == null) {
        throw new RiotApiException(404, "Аккаунт не найден");
      }
      return account;
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }
  }

  /** GET /lol/summoner/v4/summoners/by-puuid/{puuid} */
  public RiotSummonerDto getSummonerByPuuid(String puuid, String regionalUrl) {
    URI uri =
        buildUri(
            regionalUrl, "/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid);

    log.debug("Riot API: summoner by-puuid {}", puuid);

    try {
      RiotSummonerDto dto = riotRestTemplate.getForObject(uri, RiotSummonerDto.class);
      if (dto == null) {
        throw new RiotApiException(404, "Призыватель не найден");
      }
      return dto;
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    } catch (RestClientException e) {
      throw new RiotApiException(500, "Ошибка сети при запросе профиля: " + e.getMessage());
    }
  }

  /**
   * GET /lol/summoner/v4/summoners/by-name/{summonerName}
   * (fallback: /lol/summoner/v4/by-name/{summonerName})
   */
  public RiotSummonerDto getSummonerByName(String summonerName) {
    return getSummonerByName(summonerName, properties.getRegionalUrl());
  }

  public RiotSummonerDto getSummonerByName(String summonerName, String regionalUrl) {
    log.debug("Riot API: summoner by-name {}", summonerName);

    URI primaryUri =
        buildUri(
            regionalUrl,
            "/lol/summoner/v4/summoners/by-name/{name}",
            summonerName);
    URI legacyUri =
        buildUri(regionalUrl, "/lol/summoner/v4/by-name/{name}", summonerName);

    try {
      RiotSummonerDto dto = riotRestTemplate.getForObject(primaryUri, RiotSummonerDto.class);
      if (dto != null) {
        return dto;
      }
    } catch (HttpClientErrorException.NotFound e) {
      log.debug("Legacy endpoint by-name для {}", summonerName);
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }

    try {
      RiotSummonerDto dto = riotRestTemplate.getForObject(legacyUri, RiotSummonerDto.class);
      if (dto != null) {
        return dto;
      }
      throw new RiotApiException(404, "Призыватель не найден");
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    } catch (RestClientException e) {
      throw new RiotApiException(500, "Ошибка сети при запросе профиля: " + e.getMessage());
    }
  }

  public RiotLeagueEntryDto[] getLeagueEntriesBySummonerId(String summonerId) {
    return getLeagueEntriesBySummonerId(summonerId, properties.getRegionalUrl());
  }

  public RiotLeagueEntryDto[] getLeagueEntriesBySummonerId(
      String summonerId, String regionalUrl) {
    if (summonerId == null || summonerId.isBlank()) {
      return new RiotLeagueEntryDto[0];
    }

    URI uri =
        buildUri(
            regionalUrl,
            "/lol/league/v4/entries/by-summoner/{summonerId}",
            summonerId);

    log.debug("Riot API: league entries by-summoner {}", summonerId);
    return fetchLeagueEntries(uri);
  }

  /** GET /lol/league/v4/entries/by-puuid/{puuid} */
  public RiotLeagueEntryDto[] getLeagueEntriesByPuuid(String puuid) {
    return getLeagueEntriesByPuuid(puuid, properties.getRegionalUrl());
  }

  public RiotLeagueEntryDto[] getLeagueEntriesByPuuid(String puuid, String regionalUrl) {
    if (puuid == null || puuid.isBlank()) {
      return new RiotLeagueEntryDto[0];
    }

    URI uri =
        buildUri(regionalUrl, "/lol/league/v4/entries/by-puuid/{puuid}", puuid);

    log.debug("Riot API: league entries by-puuid {}", puuid);
    return fetchLeagueEntries(uri);
  }

  private RiotLeagueEntryDto[] fetchLeagueEntries(URI uri) {
    try {
      RiotLeagueEntryDto[] entries =
          riotRestTemplate.getForObject(uri, RiotLeagueEntryDto[].class);
      return entries != null ? entries : new RiotLeagueEntryDto[0];
    } catch (HttpClientErrorException.NotFound e) {
      return new RiotLeagueEntryDto[0];
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }
  }

  public String[] getMatchIds(String puuid, int start, int count) {
    return getMatchIds(puuid, start, count, properties.getPlatformUrl());
  }

  public String[] getMatchIds(String puuid, int start, int count, String platformUrl) {
    URI uri =
        UriComponentsBuilder.fromHttpUrl(platformUrl)
            .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
            .queryParam("start", start)
            .queryParam("count", count)
            .buildAndExpand(puuid)
            .encode(StandardCharsets.UTF_8)
            .toUri();

    log.debug("Riot API: match ids puuid={}, count={}", puuid, count);

    try {
      String[] ids = riotRestTemplate.getForObject(uri, String[].class);
      return ids != null ? ids : new String[0];
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }
  }

  public RiotMatchDto getMatchDetails(String matchId) {
    return getMatchDetails(matchId, properties.getPlatformUrl());
  }

  public RiotMatchDto getMatchDetails(String matchId, String platformUrl) {
    URI uri = buildUri(platformUrl, "/lol/match/v5/matches/{matchId}", matchId);

    log.debug("Riot API: match details {}", matchId);

    try {
      return riotRestTemplate.getForObject(uri, RiotMatchDto.class);
    } catch (HttpClientErrorException e) {
      throw mapHttpException(e);
    }
  }

  private static URI buildUri(String baseUrl, String pathTemplate, Object... pathVariables) {
    return UriComponentsBuilder.fromHttpUrl(baseUrl)
        .path(pathTemplate)
        .buildAndExpand(pathVariables)
        .encode(StandardCharsets.UTF_8)
        .toUri();
  }

  private RiotApiException mapHttpException(HttpClientErrorException e) {
    HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
    if (status == null) {
      status = HttpStatus.BAD_GATEWAY;
    }

    if (status == HttpStatus.TOO_MANY_REQUESTS) {
      int retryAfter = parseRetryAfter(e);
      throw new RiotRateLimitException(retryAfter);
    }

    if (status == HttpStatus.NOT_FOUND) {
      throw new RiotApiException(404, "Данные не найдены в Riot API");
    }

    throw new RiotApiException(status.value(), "Ошибка Riot API: " + status);
  }

  private static int parseRetryAfter(HttpStatusCodeException e) {
    String header =
        e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst("Retry-After") : null;
    if (header == null) {
      return 60;
    }
    try {
      return Integer.parseInt(header.trim());
    } catch (NumberFormatException ex) {
      return 60;
    }
  }
}
