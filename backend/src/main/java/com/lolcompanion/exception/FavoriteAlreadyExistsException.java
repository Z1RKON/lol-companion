package com.lolcompanion.exception;

public class FavoriteAlreadyExistsException extends RuntimeException {

  public FavoriteAlreadyExistsException() {
    super("Этот игрок уже в избранном");
  }
}
