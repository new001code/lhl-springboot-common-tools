package com.lhlwork.exception.database;

public class DatabasePropertiesBindException extends RuntimeException{
    public DatabasePropertiesBindException(String message) {
        super(message);
    }

    public DatabasePropertiesBindException(Throwable e) {
        super(e);
    }
}
