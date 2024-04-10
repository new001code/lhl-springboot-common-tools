package com.lhlwork.exception;

import java.io.Serial;

public class ServiceException extends Exception{

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable e) {
        super(e);
    }

}
