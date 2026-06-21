package com.lolcompanion.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.lolcompanion.config.JwtProperties;
import com.lolcompanion.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    JwtProperties props = new JwtProperties();
    props.setSecret("test-secret-key-for-unit-tests-min-32-chars-long!!");
    props.setExpirationMs(3_600_000L);
    jwtService = new JwtService(props);
  }

  @Test
  @DisplayName("generateToken и extractUsername")
  void generateAndExtract() {
    User user = User.register("player1", "p@mail.com", "hash");
    user.setId(42L);
    UserPrincipal principal = new UserPrincipal(user);

    String token = jwtService.generateToken(principal);

    assertThat(token).isNotBlank();
    assertThat(jwtService.extractUsername(token)).isEqualTo("player1");
    assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    assertThat(jwtService.isTokenValid(token, principal)).isTrue();
  }
}
