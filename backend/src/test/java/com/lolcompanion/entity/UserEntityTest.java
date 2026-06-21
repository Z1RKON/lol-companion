package com.lolcompanion.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntityTest {

  @Test
  @DisplayName("register создаёт пользователя")
  void register_success() {
    User user = User.register("player1", "p@mail.com", "hash");

    assertThat(user.getUsername()).isEqualTo("player1");
    assertThat(user.getEmail()).isEqualTo("p@mail.com");
    assertThat(user.getRole()).isEqualTo(User.UserRole.USER);
  }

  @Test
  @DisplayName("linkRiotAccount и hasLinkedRiotAccount")
  void linkRiotAccount() {
    User user = User.register("player1", "p@mail.com", "hash");
    assertThat(user.hasLinkedRiotAccount()).isFalse();

    user.linkRiotAccount("puuid-1", "Player#TAG", "RU");

    assertThat(user.hasLinkedRiotAccount()).isTrue();
    assertThat(user.getLinkedRiotId()).isEqualTo("Player#TAG");
    assertThat(user.getLinkedRiotRegion()).isEqualTo("RU");
  }

  @Test
  @DisplayName("unlinkRiotAccount сбрасывает привязку")
  void unlinkRiotAccount() {
    User user = User.register("player1", "p@mail.com", "hash");
    user.linkRiotAccount("puuid-1", "Player#TAG", "RU");

    user.unlinkRiotAccount();

    assertThat(user.hasLinkedRiotAccount()).isFalse();
  }

  @Test
  @DisplayName("addFavoriteSummoner")
  void addFavoriteSummoner() {
    User user = User.register("player1", "p@mail.com", "hash");
    Summoner summoner = new Summoner();
    summoner.setId(1L);
    summoner.setPuuid("p1");
    summoner.setSummonerName("Player");

    UserFavoriteSummoner link = user.addFavoriteSummoner(summoner);

    assertThat(link.getSummoner()).isEqualTo(summoner);
    assertThat(user.isSummonerInFavorites(summoner)).isTrue();
    assertThat(user.getFavoriteSummonersCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("addFavoriteSummoner: дубликат")
  void addFavoriteSummoner_duplicate() {
    User user = User.register("player1", "p@mail.com", "hash");
    Summoner summoner = new Summoner();
    summoner.setId(1L);
    summoner.setPuuid("p1");
    summoner.setSummonerName("Player");
    user.addFavoriteSummoner(summoner);

    assertThatThrownBy(() -> user.addFavoriteSummoner(summoner))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("canModerate для ролей")
  void canModerate() {
    User user = User.register("player1", "p@mail.com", "hash");
    assertThat(user.canModerate()).isFalse();
    assertThat(user.isAdmin()).isFalse();

    user.promoteTo(User.UserRole.ADMIN);
    assertThat(user.isAdmin()).isTrue();
    assertThat(user.canModerate()).isTrue();
  }
}
