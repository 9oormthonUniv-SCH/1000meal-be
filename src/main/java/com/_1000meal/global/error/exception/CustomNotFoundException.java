package com._1000meal.global.error.exception;

public class CustomNotFoundException extends CustomException {

    public CustomNotFoundException(ErrorCodeIfs errorCodeIfs) {
        super(errorCodeIfs);
    }

    public CustomNotFoundException(ErrorCodeIfs errorCodeIfs, String customMessage) {
        super(errorCodeIfs, customMessage);
    }
}
