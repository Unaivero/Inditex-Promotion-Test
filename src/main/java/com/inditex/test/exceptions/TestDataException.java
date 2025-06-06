package com.inditex.test.exceptions;

public class TestDataException extends TestFrameworkException {
    public TestDataException(String message) {
        super(message);
    }
    
    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}