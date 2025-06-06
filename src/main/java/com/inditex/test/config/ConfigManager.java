package com.inditex.test.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    private static final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    
    static {
        initializeEncryption();
        loadProperties();
    }
    
    private static void initializeEncryption() {
        String encryptionPassword = System.getenv("ENCRYPTION_PASSWORD");
        if (encryptionPassword == null) {
            encryptionPassword = "default_key_change_in_production";
            logger.warn("Using default encryption password. Set ENCRYPTION_PASSWORD environment variable for production.");
        }
        
        encryptor.setPassword(encryptionPassword);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
    }
    
    private static void loadProperties() {
        try {
            // Load main application properties
            loadPropertiesFile("application.properties");
            
            // Load environment-specific properties
            String environment = System.getProperty("test.environment", "dev");
            loadPropertiesFile("application-" + environment + ".properties");
            
            logger.info("Configuration loaded successfully for environment: {}", environment);
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }
    
    private static void loadPropertiesFile(String fileName) {
        try (InputStream inputStream = ConfigManager.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (inputStream != null) {
                Properties tempProps = new Properties();
                tempProps.load(inputStream);
                properties.putAll(tempProps);
                logger.debug("Loaded properties from: {}", fileName);
            } else {
                logger.debug("Properties file not found: {}", fileName);
            }
        } catch (IOException e) {
            logger.warn("Failed to load properties file: {}", fileName, e);
        }
    }
    
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
    
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key, defaultValue);
        }
        return value;
    }
    
    public static String getEncryptedProperty(String key) {
        return getEncryptedProperty(key, null);
    }
    
    public static String getEncryptedProperty(String key, String defaultValue) {
        String encryptedValue = getProperty(key, defaultValue);
        if (encryptedValue != null && encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
            try {
                String encrypted = encryptedValue.substring(4, encryptedValue.length() - 1);
                return encryptor.decrypt(encrypted);
            } catch (Exception e) {
                logger.error("Failed to decrypt property: {}", key, e);
                return defaultValue;
            }
        }
        return encryptedValue;
    }
    
    public static String encryptProperty(String plainText) {
        try {
            return "ENC(" + encryptor.encrypt(plainText) + ")";
        } catch (Exception e) {
            logger.error("Failed to encrypt property", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}