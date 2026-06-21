package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.lolcompanion.dto.RiotMatchDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameModeLabelsTest {

  @Test
  @DisplayName("display: известные режимы")
  void display_knownModes() {
    assertThat(GameModeLabels.display("ARAM")).isEqualTo("ARAM");
    assertThat(GameModeLabels.display("CHERRY")).isEqualTo("Arena");
    assertThat(GameModeLabels.display("CLASSIC")).isEqualTo("Summoner's Rift");
  }

  @Test
  @DisplayName("display: пустое значение")
  void display_blank() {
    assertThat(GameModeLabels.display(null)).isEqualTo("UNKNOWN");
    assertThat(GameModeLabels.display("  ")).isEqualTo("UNKNOWN");
  }

  @Test
  @DisplayName("fromRiotInfo: queueId ranked solo")
  void fromRiotInfo_rankedSolo() {
    RiotMatchDto.InfoDto info = new RiotMatchDto.InfoDto();
    info.setQueueId(420);
    info.setGameMode("CLASSIC");

    assertThat(GameModeLabels.fromRiotInfo(info)).isEqualTo("Ranked Solo/Duo");
  }

  @Test
  @DisplayName("fromRiotInfo: null")
  void fromRiotInfo_null() {
    assertThat(GameModeLabels.fromRiotInfo(null)).isEqualTo("UNKNOWN");
  }
}
