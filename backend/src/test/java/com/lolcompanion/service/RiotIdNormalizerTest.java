package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiotIdNormalizerTest {

  @Test
  @DisplayName("normalizeQuery убирает невидимые символы")
  void normalizeQuery_stripsFormatChars() {
    assertThat(RiotIdNormalizer.normalizeQuery("  Player#TAG  ")).isEqualTo("Player#TAG");
    assertThat(RiotIdNormalizer.normalizeQuery(null)).isEmpty();
  }

  @Test
  @DisplayName("parseRiotId без тега")
  void parseRiotId_withoutTag() {
    var parsed = RiotIdNormalizer.parseRiotId("PlayerOnly");

    assertThat(parsed.gameName()).isEqualTo("PlayerOnly");
    assertThat(parsed.tagLine()).isNull();
    assertThat(parsed.hasTag()).isFalse();
  }

  @Test
  @DisplayName("parseRiotId с тегом")
  void parseRiotId_withTag() {
    var parsed = RiotIdNormalizer.parseRiotId("Player#TAG");

    assertThat(parsed.gameName()).isEqualTo("Player");
    assertThat(parsed.tagLine()).isEqualTo("TAG");
    assertThat(parsed.fullRiotId()).isEqualTo("Player#TAG");
    assertThat(parsed.hasTag()).isTrue();
  }
}
