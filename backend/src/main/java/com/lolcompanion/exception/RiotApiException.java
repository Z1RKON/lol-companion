package com.lolcompanion.exception;

/**
 * Базовое исключение для общих ошибок при работе с Riot API.
 */
public class RiotApiException extends RuntimeException {
    
    private final int httpStatus;
    private final String riotErrorMessage;
    
    public RiotApiException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.riotErrorMessage = message;
    }
    
    public RiotApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 500;
        this.riotErrorMessage = message;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
    
    public String getRiotErrorMessage() {
        return riotErrorMessage;
    }
}
