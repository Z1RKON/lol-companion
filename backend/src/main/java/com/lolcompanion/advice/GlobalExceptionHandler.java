package com.lolcompanion.advice;

import com.lolcompanion.dto.api.ErrorResponseDto;
import com.lolcompanion.exception.DuplicateUserException;
import com.lolcompanion.exception.FavoriteAlreadyExistsException;
import com.lolcompanion.exception.InvalidCredentialsException;
import com.lolcompanion.exception.RiotAccountNotLinkedException;
import com.lolcompanion.exception.RiotApiException;
import com.lolcompanion.exception.RiotRateLimitException;
import com.lolcompanion.exception.SummonerNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Правило 2: единая точка обработки ошибок — JSON без stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(SummonerNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleSummonerNotFound(
      SummonerNotFoundException ex, WebRequest request) {
    log.warn("Summoner not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "SUMMONER_NOT_FOUND"));
  }

  @ExceptionHandler(RiotAccountNotLinkedException.class)
  public ResponseEntity<ErrorResponseDto> handleRiotNotLinked(
      RiotAccountNotLinkedException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "RIOT_ACCOUNT_NOT_LINKED"));
  }

  @ExceptionHandler(RiotRateLimitException.class)
  public ResponseEntity<ErrorResponseDto> handleRateLimit(
      RiotRateLimitException ex, WebRequest request) {
    log.error("Riot rate limit: retry after {}s", ex.getRetryAfterSeconds());
    ErrorResponseDto body =
        new ErrorResponseDto(
            LocalDateTime.now(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
            "Сервер перегружен запросами к Riot Games, попробуйте позже",
            extractPath(request),
            "RATE_LIMIT_EXCEEDED",
            ex.getRetryAfterSeconds());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
        .body(body);
  }

  @ExceptionHandler(RiotApiException.class)
  public ResponseEntity<ErrorResponseDto> handleRiotApi(RiotApiException ex, WebRequest request) {
    HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
    if (status == null) {
      status = HttpStatus.BAD_GATEWAY;
    }
    log.error("Riot API error {}: {}", ex.getHttpStatus(), ex.getRiotErrorMessage());

    String message =
        switch (ex.getHttpStatus()) {
          case 400 -> "Некорректный запрос. Проверьте введённые данные.";
          case 401, 403 -> "Ошибка доступа к Riot API. Повторите позже.";
          case 404 -> "Запрошенные данные не найдены в Riot API.";
          case 503 -> "Сервис Riot Games временно недоступен.";
          default -> "Ошибка при обращении к Riot Games API.";
        };

    return ResponseEntity.status(status)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                extractPath(request),
                "RIOT_API_ERROR"));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    String message =
        ex.getConstraintViolations().stream()
            .map(v -> v.getMessage())
            .collect(Collectors.joining("; "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                extractPath(request),
                "VALIDATION_ERROR"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                extractPath(request),
                "VALIDATION_ERROR"));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(
      InvalidCredentialsException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "INVALID_CREDENTIALS"));
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<ErrorResponseDto> handleDuplicateUser(
      DuplicateUserException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "DUPLICATE_USER"));
  }

  @ExceptionHandler(FavoriteAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDto> handleFavoriteExists(
      FavoriteAlreadyExistsException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "FAVORITE_ALREADY_EXISTS"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDenied(
      AccessDeniedException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "ACCESS_DENIED"));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalState(
      IllegalStateException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "BUSINESS_RULE_VIOLATION"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDto> handleBadRequest(
      IllegalArgumentException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                extractPath(request),
                "VALIDATION_ERROR"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleDataIntegrity(
      DataIntegrityViolationException ex, WebRequest request) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Не удалось сохранить профиль призывателя. Проверьте Riot ID.",
                extractPath(request),
                "DATA_INTEGRITY_ERROR"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex, WebRequest request) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponseDto.of(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Внутренняя ошибка сервера. Повторите попытку позже.",
                extractPath(request),
                "INTERNAL_SERVER_ERROR"));
  }

  private static String extractPath(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }
}
