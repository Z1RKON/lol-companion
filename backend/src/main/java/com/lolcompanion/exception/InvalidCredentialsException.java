package com.lolcompanion.exception;

public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Неверный username или пароль");
  }
}
