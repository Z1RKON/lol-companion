package com.lolcompanion.exception;

/**
 * Исключение для ошибки HTTP 429 (Too Many Requests) от Riot API.
 * Выбрасывается когда мы превысили лимит запросов к Riot Games.
 */
public class RiotRateLimitException extends RuntimeException {
    
    private final int retryAfterSeconds;
    
    public RiotRateLimitException(int retryAfterSeconds) {
        super(String.format("Превышен лимит запросов к Riot API. Повторите попытку через %d сек", retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public RiotRateLimitException(String message) {
        super(message);
        this.retryAfterSeconds = 60; // Умолчание: 60 секунд
    }
    
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
