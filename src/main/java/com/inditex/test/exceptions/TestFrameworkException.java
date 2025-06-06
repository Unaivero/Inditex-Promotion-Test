package com.inditex.test.exceptions;

public class TestFrameworkException extends RuntimeException {
    public TestFrameworkException(String message) {
        super(message);
    }
    
    public TestFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}