package com.inditex.test.exceptions;

public class WebDriverException extends TestFrameworkException {
    public WebDriverException(String message) {
        super(message);
    }
    
    public WebDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}