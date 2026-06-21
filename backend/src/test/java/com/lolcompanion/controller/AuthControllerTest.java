package com.lolcompanion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lolcompanion.advice.GlobalExceptionHandler;
import com.lolcompanion.dto.api.AuthResponseDto;
import com.lolcompanion.dto.api.UserResponseDto;
import com.lolcompanion.dto.request.LoginRequest;
import com.lolcompanion.dto.request.RegisterRequest;
import com.lolcompanion.repository.UserRepository;
import com.lolcompanion.security.JwtService;
import com.lolcompanion.service.AuthService;
import com.lolcompanion.service.UserRiotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;
  @MockBean private UserRiotService userRiotService;
  @MockBean private JwtService jwtService;
  @MockBean private UserRepository userRepository;

  @Test
  @DisplayName("POST /auth/register")
  void register_returnsCreated() throws Exception {
    UserResponseDto user =
        new UserResponseDto(1L, "player1", "p@mail.com", "USER", null, null, null);
    AuthResponseDto response = new AuthResponseDto("token", "Bearer", 3600L, user);

    when(authService.register(any(RegisterRequest.class))).thenReturn(response);

    RegisterRequest body = new RegisterRequest("player1", "p@mail.com", "secret12");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").value("token"))
        .andExpect(jsonPath("$.user.username").value("player1"));

    verify(authService).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("POST /auth/login")
  void login_returnsOk() throws Exception {
    UserResponseDto user =
        new UserResponseDto(1L, "player1", "p@mail.com", "USER", null, null, null);
    AuthResponseDto response = new AuthResponseDto("token", "Bearer", 3600L, user);

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    LoginRequest body = new LoginRequest("player1", "secret12");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("token"));

    verify(authService).login(any(LoginRequest.class));
  }
}
