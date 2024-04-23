package com.lhlwork.exception;

public class MailSenderBoundException extends Exception {
    public MailSenderBoundException(String message) {
        super(message);
    }

    public MailSenderBoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailSenderBoundException(Throwable cause) {
        super(cause);
    }
}
