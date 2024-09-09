package com.acme.rockpaperscissors.exception;

public class StatisticsNotFoundException extends RuntimeException {

    public StatisticsNotFoundException(String username) {
        super("No statistics found for user: " + username);
    }
}
