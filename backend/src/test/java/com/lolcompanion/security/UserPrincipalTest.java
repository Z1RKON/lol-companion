package com.lolcompanion.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.lolcompanion.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

  @Test
  @DisplayName("Роли и учётные данные")
  void principalFields() {
    User user = User.register("player", "p@mail.com", "encoded");
    user.setId(3L);
    UserPrincipal principal = new UserPrincipal(user);

    assertThat(principal.getId()).isEqualTo(3L);
    assertThat(principal.getEmail()).isEqualTo("p@mail.com");
    assertThat(principal.getPassword()).isEqualTo("encoded");
    assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    assertThat(principal.isAccountNonExpired()).isTrue();
    assertThat(principal.isEnabled()).isTrue();
  }
}
