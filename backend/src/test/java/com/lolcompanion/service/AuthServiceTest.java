package com.lolcompanion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lolcompanion.config.JwtProperties;
import com.lolcompanion.dto.request.LoginRequest;
import com.lolcompanion.dto.request.RegisterRequest;
import com.lolcompanion.entity.User;
import com.lolcompanion.exception.DuplicateUserException;
import com.lolcompanion.exception.InvalidCredentialsException;
import com.lolcompanion.repository.UserRepository;
import com.lolcompanion.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private JwtProperties jwtProperties;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("Регистрация: успех и JWT")
  void register_success() {
    RegisterRequest request = new RegisterRequest("player1", "p@mail.com", "secret12");

    when(userRepository.existsByUsernameIgnoreCase("player1")).thenReturn(false);
    when(userRepository.existsByEmailIgnoreCase("p@mail.com")).thenReturn(false);
    when(passwordEncoder.encode("secret12")).thenReturn("encoded");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            inv -> {
              User u = inv.getArgument(0);
              u.setId(1L);
              return u;
            });
    when(jwtService.generateToken(any())).thenReturn("jwt-token");
    when(jwtProperties.getExpirationMs()).thenReturn(86_400_000L);

    var response = authService.register(request);

    assertThat(response.accessToken()).isEqualTo("jwt-token");
    assertThat(response.user().username()).isEqualTo("player1");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Регистрация: дубликат username")
  void register_duplicateUsername() {
    when(userRepository.existsByUsernameIgnoreCase("dup")).thenReturn(true);

    assertThatThrownBy(
            () -> authService.register(new RegisterRequest("dup", "a@b.com", "secret12")))
        .isInstanceOf(DuplicateUserException.class);
  }

  @Test
  @DisplayName("Логин: неверный пароль")
  void login_wrongPassword() {
    User user = User.register("player1", "p@mail.com", "encoded");
    when(userRepository.findByUsernameOrEmail("player1")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("player1", "wrong")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  @DisplayName("Логин: по email")
  void login_byEmail() {
    User user = User.register("player1", "p@mail.com", "encoded");
    when(userRepository.findByUsernameOrEmail("p@mail.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("secret12", "encoded")).thenReturn(true);
    when(jwtService.generateToken(any())).thenReturn("jwt-token");
    when(jwtProperties.getExpirationMs()).thenReturn(86_400_000L);

    var response = authService.login(new LoginRequest("p@mail.com", "secret12"));

    assertThat(response.accessToken()).isEqualTo("jwt-token");
    assertThat(response.user().email()).isEqualTo("p@mail.com");
  }

  @Test
  @DisplayName("Регистрация: дубликат email")
  void register_duplicateEmail() {
    when(userRepository.existsByUsernameIgnoreCase("player1")).thenReturn(false);
    when(userRepository.existsByEmailIgnoreCase("p@mail.com")).thenReturn(true);

    assertThatThrownBy(
            () -> authService.register(new RegisterRequest("player1", "p@mail.com", "secret12")))
        .isInstanceOf(DuplicateUserException.class);
  }

  @Test
  @DisplayName("Логин: пользователь не найден")
  void login_userNotFound() {
    when(userRepository.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("missing", "secret12")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  @DisplayName("getUserById")
  void getUserById_success() {
    User user = User.register("player1", "p@mail.com", "hash");
    user.setId(5L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(user));

    var dto = authService.getUserById(5L);

    assertThat(dto.username()).isEqualTo("player1");
    assertThat(dto.id()).isEqualTo(5L);
  }
}
