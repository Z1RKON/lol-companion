package com.lolcompanion.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiotRegionTest {

  @Test
  @DisplayName("fromCode по коду региона")
  void fromCode_byCode() {
    assertThat(RiotRegion.fromCode("RU")).isEqualTo(RiotRegion.RU);
    assertThat(RiotRegion.fromCode("euw")).isEqualTo(RiotRegion.EUW);
  }

  @Test
  @DisplayName("fromCode по storage code")
  void fromCode_byStorageCode() {
    assertThat(RiotRegion.fromCode("EUW1")).isEqualTo(RiotRegion.EUW);
    assertThat(RiotRegion.fromCode("NA1")).isEqualTo(RiotRegion.NA);
  }

  @Test
  @DisplayName("fromCode: пустое значение → RU")
  void fromCode_blankDefaultsToRu() {
    assertThat(RiotRegion.fromCode(null)).isEqualTo(RiotRegion.RU);
    assertThat(RiotRegion.fromCode("  ")).isEqualTo(RiotRegion.RU);
  }

  @Test
  @DisplayName("fromCode: неизвестный регион")
  void fromCode_unknown() {
    assertThatThrownBy(() -> RiotRegion.fromCode("XX"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Неизвестный регион");
  }
}
