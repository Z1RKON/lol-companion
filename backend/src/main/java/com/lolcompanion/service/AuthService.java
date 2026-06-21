package com.lolcompanion.service;

import com.lolcompanion.config.JwtProperties;
import com.lolcompanion.dto.api.AuthResponseDto;
import com.lolcompanion.dto.api.UserResponseDto;
import com.lolcompanion.dto.request.LoginRequest;
import com.lolcompanion.dto.request.RegisterRequest;
import com.lolcompanion.entity.User;
import com.lolcompanion.exception.DuplicateUserException;
import com.lolcompanion.exception.InvalidCredentialsException;
import com.lolcompanion.repository.UserRepository;
import com.lolcompanion.security.JwtService;
import com.lolcompanion.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      JwtProperties jwtProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
  }

  @Transactional
  public AuthResponseDto register(RegisterRequest request) {
    if (userRepository.existsByUsernameIgnoreCase(request.username())) {
      throw new DuplicateUserException("Username уже занят");
    }
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateUserException("Email уже зарегистрирован");
    }

    String hash = passwordEncoder.encode(request.password());
    User user = User.register(request.username(), request.email(), hash);
    User saved = userRepository.save(user);

    log.info("Зарегистрирован пользователь: {}", saved.getUsername());
    return buildAuthResponse(saved);
  }

  @Transactional(readOnly = true)
  public AuthResponseDto login(LoginRequest request) {
    User user =
        userRepository
            .findByUsernameOrEmail(request.username().trim())
            .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    log.info("Успешный вход: {}", user.getUsername());
    return buildAuthResponse(user);
  }

  @Transactional(readOnly = true)
  public UserResponseDto getUserById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    return UserRiotService.toUserDto(user);
  }

  private AuthResponseDto buildAuthResponse(User user) {
    UserPrincipal principal = new UserPrincipal(user);
    String token = jwtService.generateToken(principal);
    return new AuthResponseDto(
        token, "Bearer", jwtProperties.getExpirationMs(), UserRiotService.toUserDto(user));
  }
}
