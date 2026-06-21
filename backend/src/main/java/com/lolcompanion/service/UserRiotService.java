package com.lolcompanion.service;

import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.dto.api.UserResponseDto;
import com.lolcompanion.entity.User;
import com.lolcompanion.exception.RiotAccountNotLinkedException;
import com.lolcompanion.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserRiotService {

  private final UserRepository userRepository;
  private final SummonerService summonerService;

  public UserRiotService(UserRepository userRepository, SummonerService summonerService) {
    this.userRepository = userRepository;
    this.summonerService = summonerService;
  }

  @Transactional
  public UserResponseDto linkRiotAccount(Long userId, String riotId, String regionCode) {
    SummonerResponseDto profile = summonerService.getSummonerProfileByName(riotId, regionCode);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    user.linkRiotAccount(profile.puuid(), profile.summonerName(), profile.region());
    User saved = userRepository.save(user);

    log.info("Пользователь {} привязал Riot ID {}", saved.getUsername(), saved.getLinkedRiotId());
    return toUserDto(saved);
  }

  @Transactional
  public UserResponseDto unlinkRiotAccount(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    user.unlinkRiotAccount();
    User saved = userRepository.save(user);

    log.info("Пользователь {} отвязал Riot-аккаунт", saved.getUsername());
    return toUserDto(saved);
  }

  @Transactional(readOnly = true)
  public SummonerResponseDto getLinkedSummonerProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    if (!user.hasLinkedRiotAccount()) {
      throw new RiotAccountNotLinkedException();
    }

    return summonerService.getSummonerProfileByName(
        user.getLinkedRiotId(), user.getLinkedRiotRegion());
  }

  static UserResponseDto toUserDto(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole().name(),
        user.getLinkedRiotId(),
        user.getLinkedRiotRegion(),
        user.getLinkedRiotPuuid());
  }
}
