package com.lhlwork.exception.database;

public class DatabaseConnectInitException extends RuntimeException {
    public DatabaseConnectInitException(String message) {
        super(message);
    }

    public DatabaseConnectInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseConnectInitException(Throwable cause) {
        super(cause);
    }
}
