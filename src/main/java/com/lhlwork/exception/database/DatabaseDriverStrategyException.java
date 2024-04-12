package com.lhlwork.exception.database;

public class DatabaseDriverStrategyException extends RuntimeException {
    public DatabaseDriverStrategyException(String message) {
        super(message);
    }

    public DatabaseDriverStrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseDriverStrategyException(Throwable cause) {
        super(cause);
    }
}
