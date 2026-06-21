package com.lolcompanion.exception;

public class RiotAccountNotLinkedException extends RuntimeException {
  public RiotAccountNotLinkedException() {
    super("Riot-аккаунт не привязан. Укажите Riot ID в настройках.");
  }
}
