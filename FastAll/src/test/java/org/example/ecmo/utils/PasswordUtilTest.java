package org.example.ecmo.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testEncode() {
        String rawPassword = "123456";
        String encoded = PasswordUtil.encode(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$"));
        assertEquals(60, encoded.length());
    }

    @Test
    void testMatches() {
        String rawPassword = "123456";
        String encoded = PasswordUtil.encode(rawPassword);
        
        assertTrue(PasswordUtil.matches(rawPassword, encoded));
        assertFalse(PasswordUtil.matches("wrongPassword", encoded));
    }

    @Test
    void testMatchesWithNullEncoded() {
        assertFalse(PasswordUtil.matches("123456", null));
    }

    @Test
    void testDifferentPasswordsProduceDifferentHashes() {
        String password = "123456";
        String hash1 = PasswordUtil.encode(password);
        String hash2 = PasswordUtil.encode(password);
        
        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.matches(password, hash1));
        assertTrue(PasswordUtil.matches(password, hash2));
    }
}
