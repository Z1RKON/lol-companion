package com.lolcompanion.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lolcompanion.entity.Summoner.Rank;
import com.lolcompanion.entity.Summoner.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SummonerEntityTest {

  @Test
  @DisplayName("winRate при отсутствии игр")
  void winRate_noGames() {
    Summoner summoner = new Summoner();
    assertThat(summoner.getWinRate()).isZero();
  }

  @Test
  @DisplayName("winRate при 50% побед")
  void winRate_half() {
    Summoner summoner = new Summoner();
    summoner.setWinCount(5);
    summoner.setLossCount(5);
    assertThat(summoner.getWinRate()).isEqualTo(50.0);
  }

  @Test
  @DisplayName("isCacheFresh после markRefreshed")
  void isCacheFresh_afterRefresh() {
    Summoner summoner = new Summoner();
    summoner.markRefreshed();
    assertThat(summoner.isCacheFresh(10)).isTrue();
  }

  @Test
  @DisplayName("getFullRankLabel")
  void fullRankLabel() {
    Summoner summoner = new Summoner();
    assertThat(summoner.getFullRankLabel()).isEqualTo("UNRANKED");

    summoner.updateRank(Tier.GOLD, Rank.II, 75);
    assertThat(summoner.getFullRankLabel()).isEqualTo("GOLD II");
    assertThat(summoner.isRanked()).isTrue();
  }

  @Test
  @DisplayName("updateFromRiot валидирует имя")
  void updateFromRiot_blankName() {
    Summoner summoner = new Summoner();
    assertThatThrownBy(() -> summoner.updateFromRiot("  ", 10, 1, "RU"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("recordRankedOutcome увеличивает счётчик")
  void recordRankedOutcome() {
    Summoner summoner = new Summoner();
    summoner.recordRankedOutcome(true);
    summoner.recordRankedOutcome(false);
    assertThat(summoner.getWinCount()).isEqualTo(1);
    assertThat(summoner.getLossCount()).isEqualTo(1);
  }
}
