package com.lolcompanion.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lolcompanion.exception.DuplicateUserException;
import com.lolcompanion.exception.FavoriteAlreadyExistsException;
import com.lolcompanion.exception.InvalidCredentialsException;
import com.lolcompanion.exception.SummonerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private WebRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(WebRequest.class);
    when(request.getDescription(false)).thenReturn("uri=/api/test");
  }

  @Test
  @DisplayName("SummonerNotFound → 404")
  void handleSummonerNotFound() {
    var response =
        handler.handleSummonerNotFound(
            new SummonerNotFoundException("Player", "RU"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("SUMMONER_NOT_FOUND");
  }

  @Test
  @DisplayName("InvalidCredentials → 401")
  void handleInvalidCredentials() {
    var response =
        handler.handleInvalidCredentials(new InvalidCredentialsException(), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
  }

  @Test
  @DisplayName("DuplicateUser → 409")
  void handleDuplicateUser() {
    var response =
        handler.handleDuplicateUser(new DuplicateUserException("username"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().code()).isEqualTo("DUPLICATE_USER");
  }

  @Test
  @DisplayName("FavoriteAlreadyExists → 409")
  void handleFavoriteExists() {
    var response = handler.handleFavoriteExists(new FavoriteAlreadyExistsException(), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().code()).isEqualTo("FAVORITE_ALREADY_EXISTS");
  }

  @Test
  @DisplayName("IllegalArgumentException → 400")
  void handleBadRequest() {
    var response =
        handler.handleBadRequest(new IllegalArgumentException("Некорректные данные"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message()).contains("Некорректные данные");
  }
}
