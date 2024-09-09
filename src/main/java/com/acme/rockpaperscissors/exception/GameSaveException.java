package com.acme.rockpaperscissors.exception;

public class GameSaveException extends RuntimeException {
    public GameSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
