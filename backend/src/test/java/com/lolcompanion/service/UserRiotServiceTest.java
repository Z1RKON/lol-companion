package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.entity.User;
import com.lolcompanion.exception.RiotAccountNotLinkedException;
import com.lolcompanion.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRiotServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private SummonerService summonerService;

  @InjectMocks private UserRiotService userRiotService;

  @Test
  @DisplayName("Привязка Riot ID")
  void linkRiotAccount_success() {
    User user = userWithId(1L);
    SummonerResponseDto profile =
        new SummonerResponseDto(10L, "puuid-1", "Player#TAG", 100, 1, "GOLD", "II", 50, "50.0%", "RU");

    when(summonerService.getSummonerProfileByName("Player#TAG", "RU")).thenReturn(profile);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    var result = userRiotService.linkRiotAccount(1L, "Player#TAG", "RU");

    assertThat(result.linkedRiotId()).isEqualTo("Player#TAG");
    assertThat(result.linkedRiotPuuid()).isEqualTo("puuid-1");
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Отвязка Riot ID")
  void unlinkRiotAccount_success() {
    User user = userWithId(1L);
    user.linkRiotAccount("puuid-1", "Player#TAG", "RU");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    var result = userRiotService.unlinkRiotAccount(1L);

    assertThat(result.linkedRiotId()).isNull();
    assertThat(user.hasLinkedRiotAccount()).isFalse();
  }

  @Test
  @DisplayName("Профиль без привязки → исключение")
  void getLinkedSummonerProfile_notLinked() {
    User user = userWithId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userRiotService.getLinkedSummonerProfile(1L))
        .isInstanceOf(RiotAccountNotLinkedException.class);
  }

  @Test
  @DisplayName("Профиль привязанного аккаунта")
  void getLinkedSummonerProfile_success() {
    User user = userWithId(1L);
    user.linkRiotAccount("puuid-1", "Player#TAG", "RU");
    SummonerResponseDto profile =
        new SummonerResponseDto(10L, "puuid-1", "Player#TAG", 100, 1, "GOLD", "II", 50, "50.0%", "RU");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(summonerService.getSummonerProfileByName("Player#TAG", "RU")).thenReturn(profile);

    var result = userRiotService.getLinkedSummonerProfile(1L);

    assertThat(result.puuid()).isEqualTo("puuid-1");
    verify(summonerService).getSummonerProfileByName(eq("Player#TAG"), eq("RU"));
  }

  private static User userWithId(Long id) {
    User user = User.register("player", "p@mail.com", "hash");
    user.setId(id);
    return user;
  }
}
