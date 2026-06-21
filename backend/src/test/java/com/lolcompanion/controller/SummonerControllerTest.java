package com.lolcompanion.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lolcompanion.advice.GlobalExceptionHandler;
import com.lolcompanion.dto.api.FavoriteSummonerDto;
import com.lolcompanion.dto.api.MatchHistoryItemDto;
import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.dto.request.AddFavoriteRequest;
import com.lolcompanion.entity.User;
import com.lolcompanion.repository.UserRepository;
import com.lolcompanion.security.JwtService;
import com.lolcompanion.security.UserPrincipal;
import com.lolcompanion.service.FavoriteService;
import com.lolcompanion.service.MatchService;
import com.lolcompanion.service.SummonerService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = SummonerController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SummonerControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private SummonerService summonerService;
  @MockBean private MatchService matchService;
  @MockBean private FavoriteService favoriteService;
  @MockBean private JwtService jwtService;
  @MockBean private UserRepository userRepository;

  @BeforeEach
  void authenticateTestUser() {
    User user = User.register("testuser", "test@example.com", "hash");
    user.setId(1L);
    UserPrincipal principal = new UserPrincipal(user);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("GET /summoner/search — успешный поиск")
  void searchSummoner_returnsProfile() throws Exception {
    SummonerResponseDto dto =
        new SummonerResponseDto(
            1L, "puuid-1", "PlayerOne", 500, 1, "CHALLENGER", "I", 100, "55.0%", "EUW1");

    when(summonerService.getSummonerProfileByName("PlayerOne", "RU")).thenReturn(dto);

    mockMvc
        .perform(get("/summoner/search").param("name", "PlayerOne"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summonerName").value("PlayerOne"))
        .andExpect(jsonPath("$.puuid").value("puuid-1"));

    verify(summonerService).getSummonerProfileByName("PlayerOne", "RU");
  }

  @Test
  @DisplayName("GET /summoner/search — пустой name → 400")
  void searchSummoner_blankName_returnsBadRequest() throws Exception {
    mockMvc.perform(get("/summoner/search").param("name", "   ")).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /summoner/by-name/{name}")
  void getByName_returnsProfile() throws Exception {
    SummonerResponseDto dto =
        new SummonerResponseDto(
            2L, "puuid-2", "PlayerTwo", 400, 2, "DIAMOND", "II", 75, "52.0%", "EUW1");

    when(summonerService.getSummonerProfileByName("PlayerTwo", "RU")).thenReturn(dto);

    mockMvc
        .perform(get("/summoner/by-name/PlayerTwo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summonerName").value("PlayerTwo"));
  }

  @Test
  @DisplayName("GET /summoner/{puuid} — профиль по PUUID")
  void getByPuuid_returnsProfile() throws Exception {
    SummonerResponseDto dto =
        new SummonerResponseDto(
            1L, "puuid-1", "PlayerOne", 500, 1, "CHALLENGER", "I", 100, "55.0%", "RU");

    when(summonerService.getSummonerProfileByPuuid("puuid-1-abcdefghij")).thenReturn(dto);

    mockMvc
        .perform(get("/summoner/puuid-1-abcdefghij"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.puuid").value("puuid-1"))
        .andExpect(jsonPath("$.summonerName").value("PlayerOne"));

    verify(summonerService).getSummonerProfileByPuuid("puuid-1-abcdefghij");
  }

  @Test
  @DisplayName("GET /summoner/{puuid}/matches — история матчей")
  void getMatchHistory_returnsList() throws Exception {
    MatchHistoryItemDto match =
        new MatchHistoryItemDto(
            "EUW1_123",
            "RANKED_SOLO_5x5",
            25,
            1_700_000_000_000L,
            "Ahri",
            "14.6.1",
            5,
            2,
            8,
            "6.5",
            200,
            12_000L,
            true,
            List.of(3157, 3089, 3020, 0, 0, 0));

    when(matchService.getMatchHistory("puuid-1", 20)).thenReturn(List.of(match));

    mockMvc
        .perform(get("/summoner/puuid-1/matches").param("count", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].championName").value("Ahri"))
        .andExpect(jsonPath("$[0].win").value(true));
  }

  @Test
  @DisplayName("POST /summoner/favorites — добавление в избранное")
  void addFavorite_returnsCreated() throws Exception {
    FavoriteSummonerDto favorite =
        new FavoriteSummonerDto(
            10L,
            1L,
            "puuid-1",
            "PlayerOne",
            500,
            "CHALLENGER",
            "I",
            100,
            "55.0%",
            LocalDateTime.now());

    when(favoriteService.addFavorite(1L, "puuid-1-abcdefghij")).thenReturn(favorite);

    AddFavoriteRequest body = new AddFavoriteRequest("puuid-1-abcdefghij");

    mockMvc
        .perform(
            post("/summoner/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.summonerName").value("PlayerOne"))
        .andExpect(jsonPath("$.puuid").value("puuid-1"));

    verify(favoriteService).addFavorite(1L, "puuid-1-abcdefghij");
  }

  @Test
  @DisplayName("GET /summoner/favorites — список избранного")
  void listFavorites_returnsList() throws Exception {
    FavoriteSummonerDto fav =
        new FavoriteSummonerDto(
            1L, 1L, "p1", "PlayerOne", 500, "GOLD", "I", 50, "50.0%", LocalDateTime.now());

    when(favoriteService.listFavorites(1L)).thenReturn(List.of(fav));

    mockMvc
        .perform(get("/summoner/favorites"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].summonerName").value("PlayerOne"));
  }

  @Test
  @DisplayName("DELETE /summoner/favorites/{summonerId}")
  void removeFavorite_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/summoner/favorites/5")).andExpect(status().isNoContent());

    verify(favoriteService).removeFavorite(1L, 5L);
  }

  @Test
  @DisplayName("POST /summoner/{puuid}/refresh")
  void refreshSummoner_returnsOk() throws Exception {
    SummonerResponseDto dto =
        new SummonerResponseDto(
            1L, "puuid-1", "PlayerOne", 501, 1, "CHALLENGER", "I", 100, "55.0%", "EUW1");

    when(summonerService.forceRefreshSummonerDto("puuid-1")).thenReturn(dto);

    mockMvc
        .perform(post("/summoner/puuid-1/refresh"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summonerName").value("PlayerOne"));
  }
}
