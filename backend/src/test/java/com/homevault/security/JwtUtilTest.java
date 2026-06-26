package com.homevault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String TEST_SECRET =
            "c2VjcmV0LWtleS1mb3ItdGVzdGluZy1vbmx5LW5vdC1mb3ItcHJvZHVjdGlvbg==";
    private static final long EXPIRATION_MS = 86_400_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("user@example.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String email = "sagarika@example.com";
        String token = jwtUtil.generateToken(email);
        assertEquals(email, jwtUtil.getEmailFromToken(token));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken("user@example.com");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken("user@example.com");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void validateToken_returnsFalseForEmptyString() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void generateToken_differentEmailsProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken("a@example.com");
        String token2 = jwtUtil.generateToken("b@example.com");
        assertNotEquals(token1, token2);
    }
}
