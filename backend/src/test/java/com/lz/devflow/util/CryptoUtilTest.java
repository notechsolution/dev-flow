package com.lz.devflow.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CryptoUtil
 */
class CryptoUtilTest {
    
    @Test
    void testEncryptDecrypt() {
        String plainText = "ghp_myGitHubToken123456789";
        
        // Encrypt
        String encrypted = CryptoUtil.encrypt(plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // Decrypt
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testEncryptNullOrEmpty() {
        // Null should return null
        assertNull(CryptoUtil.encrypt(null));
        assertNull(CryptoUtil.decrypt(null));
        
        // Empty string should return empty string
        assertEquals("", CryptoUtil.encrypt(""));
        assertEquals("", CryptoUtil.decrypt(""));
    }
    
    @Test
    void testEncryptComplexString() {
        String complexToken = "username:password@123!#$%";
        
        String encrypted = CryptoUtil.encrypt(complexToken);
        String decrypted = CryptoUtil.decrypt(encrypted);
        
        assertEquals(complexToken, decrypted);
    }
    
    @Test
    void testEncryptLongString() {
        String longToken = "very_long_token_".repeat(100);
        
        String encrypted = CryptoUtil.encrypt(longToken);
        String decrypted = CryptoUtil.decrypt(encrypted);
        
        assertEquals(longToken, decrypted);
    }
    
    @Test
    void testConsistentEncryption() {
        String plainText = "test_token_123";
        
        // Same input should produce same encrypted output
        String encrypted1 = CryptoUtil.encrypt(plainText);
        String encrypted2 = CryptoUtil.encrypt(plainText);
        
        assertEquals(encrypted1, encrypted2);
    }
}
