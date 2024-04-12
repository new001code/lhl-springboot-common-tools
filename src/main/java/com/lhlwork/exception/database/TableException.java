package com.lhlwork.exception.database;

public class TableException extends RuntimeException {
    public TableException(String message) {
        super(message);
    }

    public TableException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableException(Throwable e) {
        super(e);
    }
}
