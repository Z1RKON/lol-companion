package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lolcompanion.config.RiotApiProperties;
import com.lolcompanion.dto.RiotLeagueEntryDto;
import com.lolcompanion.dto.RiotSummonerDto;
import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.Summoner.Tier;
import com.lolcompanion.exception.RiotApiException;
import com.lolcompanion.exception.SummonerNotFoundException;
import com.lolcompanion.repository.SummonerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SummonerServiceTest {

  @Mock private SummonerRepository summonerRepository;
  @Mock private RiotApiClient riotApiClient;
  @Mock private RiotApiProperties riotApiProperties;

  @InjectMocks private SummonerService summonerService;

  @BeforeEach
  void setUp() {
    lenient().when(riotApiProperties.getCacheTtlMinutes()).thenReturn(10);
    lenient().when(riotApiProperties.getDefaultRegion()).thenReturn("EUW1");
  }

  @Test
  @DisplayName("Cache hit: данные из БД без вызова Riot API")
  void getSummonerByName_whenCacheFresh_returnsFromDbWithoutRiotCall() {
    Summoner cached = buildSummoner("PlayerOne", "puuid-1");
    cached.markRefreshed();

    when(summonerRepository.findBySummonerNameIgnoreCaseAndRegion("PlayerOne", "RU"))
        .thenReturn(Optional.of(cached));

    Summoner result = summonerService.getSummonerByName("PlayerOne");

    assertThat(result.getSummonerName()).isEqualTo("PlayerOne");
    verify(riotApiClient, never()).getSummonerByName(anyString(), anyString());
    verify(summonerRepository, never()).save(any());
  }

  @Test
  @DisplayName("Cache miss: запрос к Riot API и сохранение в БД")
  void getSummonerByName_whenCacheStale_fetchesFromRiotAndSaves() {
    Summoner stale = buildSummoner("PlayerOne", "puuid-1");
    stale.setLastUpdated(java.time.LocalDateTime.now().minusHours(2));

    RiotSummonerDto riotDto = new RiotSummonerDto();
    riotDto.setPuuid("puuid-1");
    riotDto.setSummonerName("PlayerOne");
    riotDto.setSummonerLevel(500);
    riotDto.setProfileIconId(1);
    riotDto.setSummonerId("enc-id");

    when(summonerRepository.findBySummonerNameIgnoreCaseAndRegion("PlayerOne", "RU"))
        .thenReturn(Optional.of(stale));
    when(riotApiClient.getSummonerByName(eq("PlayerOne"), anyString())).thenReturn(riotDto);
    when(riotApiClient.getLeagueEntriesByPuuid(eq("puuid-1"), anyString()))
        .thenReturn(new RiotLeagueEntryDto[0]);
    when(summonerRepository.save(any(Summoner.class))).thenAnswer(inv -> inv.getArgument(0));

    Summoner result = summonerService.getSummonerByName("PlayerOne");

    assertThat(result.getSummonerLevel()).isEqualTo(500);
    verify(riotApiClient).getSummonerByName(eq("PlayerOne"), anyString());
    verify(summonerRepository).save(stale);
  }

  @Test
  @DisplayName("Riot 404: SummonerNotFoundException")
  void getSummonerByName_whenRiotReturns404_throwsNotFound() {
    when(summonerRepository.findBySummonerNameIgnoreCaseAndRegion("Unknown", "RU"))
        .thenReturn(Optional.empty());
    when(riotApiClient.getSummonerByName(eq("Unknown"), anyString()))
        .thenThrow(new RiotApiException(404, "Не найден"));

    assertThatThrownBy(() -> summonerService.getSummonerByName("Unknown"))
        .isInstanceOf(SummonerNotFoundException.class);
  }

  @Test
  @DisplayName("getSummonerProfileByName возвращает DTO")
  void getSummonerProfileByName_returnsDto() {
    Summoner cached = buildSummoner("PlayerTwo", "puuid-2");
    cached.markRefreshed();

    when(summonerRepository.findBySummonerNameIgnoreCaseAndRegion("PlayerTwo", "RU"))
        .thenReturn(Optional.of(cached));

    SummonerResponseDto dto = summonerService.getSummonerProfileByName("PlayerTwo");

    assertThat(dto.summonerName()).isEqualTo("PlayerTwo");
    assertThat(dto.puuid()).isEqualTo("puuid-2");
    verify(riotApiClient, never()).getSummonerByName(anyString(), anyString());
  }

  @Test
  @DisplayName("Новый игрок: создаётся запись в БД после Riot")
  void getSummonerByName_whenNotInDb_createsNewSummoner() {
    RiotSummonerDto riotDto = new RiotSummonerDto();
    riotDto.setPuuid("new-puuid");
    riotDto.setSummonerName("NewPlayer");
    riotDto.setSummonerLevel(30);
    riotDto.setSummonerId("sid-1");

    when(summonerRepository.findBySummonerNameIgnoreCaseAndRegion("NewPlayer", "RU"))
        .thenReturn(Optional.empty());
    when(riotApiClient.getSummonerByName(eq("NewPlayer"), anyString())).thenReturn(riotDto);
    when(riotApiClient.getLeagueEntriesByPuuid(eq("new-puuid"), anyString()))
        .thenReturn(new RiotLeagueEntryDto[0]);
    when(summonerRepository.save(any(Summoner.class))).thenAnswer(inv -> inv.getArgument(0));

    Summoner result = summonerService.getSummonerByName("NewPlayer");

    assertThat(result.getPuuid()).isEqualTo("new-puuid");
    verify(riotApiClient).getSummonerByName(eq("NewPlayer"), anyString());
  }

  @Test
  @DisplayName("getSummonerByPuuid: профиль из БД")
  void getSummonerByPuuid_returnsFromDb() {
    Summoner cached = buildSummoner("Cached", "puuid-cached");
    when(summonerRepository.findByPuuid("puuid-cached")).thenReturn(Optional.of(cached));

    Summoner result = summonerService.getSummonerByPuuid("puuid-cached");

    assertThat(result.getPuuid()).isEqualTo("puuid-cached");
  }

  @Test
  @DisplayName("getSummonerByPuuid: не найден")
  void getSummonerByPuuid_notFound() {
    when(summonerRepository.findByPuuid("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> summonerService.getSummonerByPuuid("missing"))
        .isInstanceOf(SummonerNotFoundException.class);
  }

  private static Summoner buildSummoner(String name, String puuid) {
    Summoner summoner = new Summoner();
    summoner.setId(1L);
    summoner.setSummonerName(name);
    summoner.setPuuid(puuid);
    summoner.setSummonerLevel(100);
    summoner.setProfileIconId(1);
    summoner.setTier(Tier.GOLD);
    summoner.setRegion("RU");
    summoner.setLeaguePoints(50);
    summoner.setWinCount(10);
    summoner.setLossCount(10);
    return summoner;
  }
}
