package com.cocodan.triplan.exception;

import com.cocodan.triplan.exception.common.NotIncludeException;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ExceptionResponse {

    private String message;
    private Map<String, String> errors;

    private ExceptionResponse(String message) {
        this.message = message;
    }

    private ExceptionResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }

    public static ExceptionResponse from(String message, Map<String, String> errors) {
        return new ExceptionResponse(message, errors);
    }

    public static ExceptionResponse from(String message) {
        return new ExceptionResponse(message);
    }
}
