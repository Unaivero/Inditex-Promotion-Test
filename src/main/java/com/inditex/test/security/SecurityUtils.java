package com.inditex.test.security;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class SecurityUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    // Common XSS patterns to detect
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<.*?javascript:.*?>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<.*?onload.*?=.*?>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<.*?onerror.*?=.*?>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<.*?onclick.*?=.*?>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE)
    };
    
    // SQL Injection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%27)|(%)|(\\+))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("((\\%3D)|(=))[^\\n]*((\\%27)|(\\')|(\\-\\-)|(\\%3B)|(;))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\w*((%27)|(\\'))((\\%6F)|(o)|(\\%4F))((\\%72)|(r)|(\\%52))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("((\\%27)|(\\'))union", Pattern.CASE_INSENSITIVE)
    };
    
    public static String sanitizeForHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }
    
    public static String sanitizeForHtmlAttribute(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtmlAttribute(input);
    }
    
    public static String sanitizeForJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }
    
    public static String sanitizeForCss(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forCssString(input);
    }
    
    public static String sanitizeForUrl(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forUriComponent(input);
    }
    
    public static boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logger.warn("Potential XSS detected in input: {}", input.substring(0, Math.min(input.length(), 50)));
                return true;
            }
        }
        return false;
    }
    
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logger.warn("Potential SQL injection detected in input: {}", input.substring(0, Math.min(input.length(), 50)));
                return true;
            }
        }
        return false;
    }
    
    public static void validateInput(String input, String fieldName) throws SecurityException {
        if (input == null) {
            return;
        }
        
        if (containsXss(input)) {
            throw new SecurityException("XSS attempt detected in field: " + fieldName);
        }
        
        if (containsSqlInjection(input)) {
            throw new SecurityException("SQL injection attempt detected in field: " + fieldName);
        }
    }
    
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // Basic URL validation
        String urlPattern = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(urlPattern);
        return pattern.matcher(url).matches();
    }
    
    public static String maskSensitiveData(String input) {
        if (input == null || input.length() <= 4) {
            return "****";
        }
        return input.substring(0, 2) + "****" + input.substring(input.length() - 2);
    }
}