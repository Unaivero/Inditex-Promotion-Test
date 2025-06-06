package com.inditex.test.exceptions;

public class PageObjectException extends TestFrameworkException {
    public PageObjectException(String message) {
        super(message);
    }
    
    public PageObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}