package com.iitp.global.exception;

import lombok.Getter;

@Getter
public class RefoodException extends RuntimeException {
    private final ExceptionMessage exceptionMessage;

    public RefoodException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.exceptionMessage = exceptionMessage;
    }
}
