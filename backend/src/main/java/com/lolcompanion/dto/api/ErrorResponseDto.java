package com.lolcompanion.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    String code,
    Integer retryAfterSeconds) {

  public static ErrorResponseDto of(
      LocalDateTime timestamp,
      int status,
      String error,
      String message,
      String path,
      String code) {
    return new ErrorResponseDto(timestamp, status, error, message, path, code, null);
  }
}
