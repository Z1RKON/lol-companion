package com.lolcompanion.service;

import com.lolcompanion.dto.api.FavoriteSummonerDto;
import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.User;
import com.lolcompanion.entity.UserFavoriteSummoner;
import com.lolcompanion.exception.FavoriteAlreadyExistsException;
import com.lolcompanion.exception.SummonerNotFoundException;
import com.lolcompanion.repository.SummonerRepository;
import com.lolcompanion.repository.UserFavoriteSummonerRepository;
import com.lolcompanion.repository.UserRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class FavoriteService {

  private static final int MAX_FAVORITES = 50;

  private final UserRepository userRepository;
  private final SummonerRepository summonerRepository;
  private final UserFavoriteSummonerRepository favoriteRepository;
  public FavoriteService(
      UserRepository userRepository,
      SummonerRepository summonerRepository,
      UserFavoriteSummonerRepository favoriteRepository) {
    this.userRepository = userRepository;
    this.summonerRepository = summonerRepository;
    this.favoriteRepository = favoriteRepository;
  }

  public List<FavoriteSummonerDto> listFavorites(Long userId) {
    User user = loadUser(userId);
    return favoriteRepository.findByUserWithSummoner(user).stream()
        .map(this::toDto)
        .toList();
  }

  public FavoriteSummonerDto addFavorite(Long userId, String puuid) {
    User user = loadUser(userId);

    if (favoriteRepository.countByUser(user) >= MAX_FAVORITES) {
      throw new IllegalStateException(
          "Максимум " + MAX_FAVORITES + " игроков в избранном. Удалите кого-нибудь.");
    }

    Summoner summoner =
        summonerRepository
            .findByPuuid(puuid)
            .orElseThrow(() -> new SummonerNotFoundException("PUUID: " + puuid));

    if (favoriteRepository.existsByUserAndSummoner(user, summoner)) {
      throw new FavoriteAlreadyExistsException();
    }

    UserFavoriteSummoner link = user.addFavoriteSummoner(summoner);
    UserFavoriteSummoner saved = favoriteRepository.save(link);
    log.info("Пользователь {} добавил в избранное puuid={}", userId, puuid);
    return toDto(saved);
  }

  public void removeFavorite(Long userId, Long summonerId) {
    User user = loadUser(userId);
    Summoner summoner =
        summonerRepository
            .findById(summonerId)
            .orElseThrow(() -> new SummonerNotFoundException("ID: " + summonerId));

    if (!favoriteRepository.existsByUserAndSummoner(user, summoner)) {
      throw new IllegalArgumentException("Призыватель не в избранном");
    }

    favoriteRepository.deleteByUserAndSummoner(user, summoner);
    user.removeFavoriteSummoner(summoner);
    log.info("Пользователь {} удалил из избранного summonerId={}", userId, summonerId);
  }

  private User loadUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
  }

  private FavoriteSummonerDto toDto(UserFavoriteSummoner link) {
    Summoner s = link.getSummoner();
    return new FavoriteSummonerDto(
        link.getId(),
        s.getId(),
        s.getPuuid(),
        s.getSummonerName(),
        s.getSummonerLevel(),
        s.getTier() != null ? s.getTier().name() : "UNRANKED",
        s.getRank() != null ? s.getRank().name() : null,
        s.getLeaguePoints(),
        String.format("%.1f%%", s.getWinRate()),
        link.getAddedAt());
  }
}
