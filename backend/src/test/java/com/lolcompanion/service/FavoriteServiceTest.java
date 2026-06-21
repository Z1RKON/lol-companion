package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.Summoner.Tier;
import com.lolcompanion.entity.User;
import com.lolcompanion.entity.UserFavoriteSummoner;
import com.lolcompanion.exception.FavoriteAlreadyExistsException;
import com.lolcompanion.exception.SummonerNotFoundException;
import com.lolcompanion.repository.SummonerRepository;
import com.lolcompanion.repository.UserFavoriteSummonerRepository;
import com.lolcompanion.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private SummonerRepository summonerRepository;
  @Mock private UserFavoriteSummonerRepository favoriteRepository;

  @InjectMocks private FavoriteService favoriteService;

  @Test
  @DisplayName("Список избранного")
  void listFavorites_returnsDtos() {
    User user = userWithId(1L);
    Summoner summoner = summonerWithId(10L, "puuid-1", "PlayerOne");
    UserFavoriteSummoner link = UserFavoriteSummoner.create(user, summoner);
    link.setId(100L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(favoriteRepository.findByUserWithSummoner(user)).thenReturn(List.of(link));

    var result = favoriteService.listFavorites(1L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).puuid()).isEqualTo("puuid-1");
    assertThat(result.get(0).summonerName()).isEqualTo("PlayerOne");
  }

  @Test
  @DisplayName("Добавление в избранное по PUUID")
  void addFavorite_success() {
    User user = userWithId(1L);
    Summoner summoner = summonerWithId(5L, "puuid-1", "PlayerOne");
    UserFavoriteSummoner link = UserFavoriteSummoner.create(user, summoner);
    link.setId(50L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(favoriteRepository.countByUser(user)).thenReturn(0L);
    when(summonerRepository.findByPuuid("puuid-1")).thenReturn(Optional.of(summoner));
    when(favoriteRepository.existsByUserAndSummoner(user, summoner)).thenReturn(false);
    when(favoriteRepository.save(any(UserFavoriteSummoner.class))).thenReturn(link);

    var result = favoriteService.addFavorite(1L, "puuid-1");

    assertThat(result.puuid()).isEqualTo("puuid-1");
    verify(favoriteRepository).save(any(UserFavoriteSummoner.class));
  }

  @Test
  @DisplayName("Дубликат в избранном")
  void addFavorite_alreadyExists() {
    User user = userWithId(1L);
    Summoner summoner = summonerWithId(5L, "puuid-1", "PlayerOne");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(favoriteRepository.countByUser(user)).thenReturn(0L);
    when(summonerRepository.findByPuuid("puuid-1")).thenReturn(Optional.of(summoner));
    when(favoriteRepository.existsByUserAndSummoner(user, summoner)).thenReturn(true);

    assertThatThrownBy(() -> favoriteService.addFavorite(1L, "puuid-1"))
        .isInstanceOf(FavoriteAlreadyExistsException.class);
    verify(favoriteRepository, never()).save(any());
  }

  @Test
  @DisplayName("PUUID не найден в БД")
  void addFavorite_summonerNotFound() {
    User user = userWithId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(favoriteRepository.countByUser(user)).thenReturn(0L);
    when(summonerRepository.findByPuuid("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> favoriteService.addFavorite(1L, "missing"))
        .isInstanceOf(SummonerNotFoundException.class);
  }

  @Test
  @DisplayName("Удаление из избранного")
  void removeFavorite_success() {
    User user = userWithId(1L);
    Summoner summoner = summonerWithId(5L, "puuid-1", "PlayerOne");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(summonerRepository.findById(5L)).thenReturn(Optional.of(summoner));
    when(favoriteRepository.existsByUserAndSummoner(user, summoner)).thenReturn(true);

    favoriteService.removeFavorite(1L, 5L);

    verify(favoriteRepository).deleteByUserAndSummoner(user, summoner);
  }

  private static User userWithId(Long id) {
    User user = User.register("player", "p@mail.com", "hash");
    user.setId(id);
    return user;
  }

  private static Summoner summonerWithId(Long id, String puuid, String name) {
    Summoner summoner = new Summoner();
    summoner.setId(id);
    summoner.setPuuid(puuid);
    summoner.setSummonerName(name);
    summoner.setSummonerLevel(100);
    summoner.setProfileIconId(1);
    summoner.setTier(Tier.GOLD);
    summoner.setRegion("RU");
    summoner.setLeaguePoints(50);
    summoner.setWinCount(5);
    summoner.setLossCount(5);
    return summoner;
  }
}
