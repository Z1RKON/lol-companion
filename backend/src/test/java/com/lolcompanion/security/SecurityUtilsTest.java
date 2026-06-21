package com.lolcompanion.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lolcompanion.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsTest {

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("getCurrentUserId при авторизованном пользователе")
  void getCurrentUserId_authenticated() {
    User user = User.register("player", "p@mail.com", "hash");
    user.setId(7L);
    UserPrincipal principal = new UserPrincipal(user);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

    assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(7L);
    assertThat(SecurityUtils.getCurrentUser().getUsername()).isEqualTo("player");
  }

  @Test
  @DisplayName("getCurrentUser без авторизации")
  void getCurrentUser_unauthenticated() {
    assertThatThrownBy(SecurityUtils::getCurrentUser)
        .isInstanceOf(AccessDeniedException.class);
  }
}
