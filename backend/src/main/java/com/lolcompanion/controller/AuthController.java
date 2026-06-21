package com.lolcompanion.controller;

import com.lolcompanion.dto.api.AuthResponseDto;
import com.lolcompanion.dto.api.SummonerResponseDto;
import com.lolcompanion.dto.api.UserResponseDto;
import com.lolcompanion.dto.request.LinkRiotAccountRequest;
import com.lolcompanion.dto.request.LoginRequest;
import com.lolcompanion.dto.request.RegisterRequest;
import com.lolcompanion.security.SecurityUtils;
import com.lolcompanion.service.AuthService;
import com.lolcompanion.service.UserRiotService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Control: регистрация, авторизация, профиль текущего пользователя. */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;
  private final UserRiotService userRiotService;

  public AuthController(AuthService authService, UserRiotService userRiotService) {
    this.authService = authService;
    this.userRiotService = userRiotService;
  }

  /** POST /api/auth/register */
  @PostMapping("/register")
  public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  /** POST /api/auth/login */
  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  /** GET /api/auth/me */
  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> me() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(authService.getUserById(userId));
  }

  /** PUT /api/auth/me/riot-account — привязать свой Riot ID */
  @PutMapping("/me/riot-account")
  public ResponseEntity<UserResponseDto> linkRiotAccount(
      @Valid @RequestBody LinkRiotAccountRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        userRiotService.linkRiotAccount(userId, request.riotId(), request.region()));
  }

  /** DELETE /api/auth/me/riot-account */
  @DeleteMapping("/me/riot-account")
  public ResponseEntity<UserResponseDto> unlinkRiotAccount() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(userRiotService.unlinkRiotAccount(userId));
  }

  /** GET /api/auth/me/summoner — профиль привязанного Riot-аккаунта */
  @GetMapping("/me/summoner")
  public ResponseEntity<SummonerResponseDto> mySummoner() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(userRiotService.getLinkedSummonerProfile(userId));
  }
}
