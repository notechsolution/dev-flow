package com.lz.devflow.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Crypto utility for encrypting and decrypting sensitive data
 */
@Component
public class CryptoUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    private static String secretKey;
    
    @Value("${crypto.secret.key:DevFlowSecretKey1234567890123456}")
    public void setSecretKey(String key) {
        secretKey = key;
    }
    
    /**
     * Encrypt plain text using AES
     * @param plainText text to encrypt
     * @return encrypted text in Base64 format
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // Ensure key is 16, 24, or 32 bytes for AES
            byte[] keyBytes = getValidKeyBytes(secretKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }
    
    /**
     * Decrypt encrypted text using AES
     * @param encryptedText encrypted text in Base64 format
     * @return decrypted plain text
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // Ensure key is 16, 24, or 32 bytes for AES
            byte[] keyBytes = getValidKeyBytes(secretKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
    
    /**
     * Ensure the key is valid for AES (16, 24, or 32 bytes)
     * @param key original key
     * @return valid key bytes
     */
    private static byte[] getValidKeyBytes(String key) {
        if (key == null || key.isEmpty()) {
            key = "DevFlowSecretKey1234567890123456"; // Default key
        }
        
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        // Use 32 bytes for AES-256
        byte[] validKeyBytes = new byte[32];
        System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, 32));
        
        return validKeyBytes;
    }
}
