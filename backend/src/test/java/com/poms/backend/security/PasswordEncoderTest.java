package com.poms.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Encoded password can be successfully matched against the raw password")
    void encode_AndMatch_Success() {
        String rawPassword = "ValidSecurePassword#2026";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$"), "Hash should start with BCrypt identifier $2a$");
        assertEquals(60, encoded.length(), "BCrypt hash length should be 60 characters");
        assertTrue(passwordEncoder.matches(rawPassword, encoded), "Encoder should match raw password with generated hash");
    }

    @Test
    @DisplayName("Encoder rejects incorrect raw password")
    void matches_WithIncorrectPassword_ReturnsFalse() {
        String rawPassword = "CorrectPassword123";
        String wrongPassword = "IncorrectPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encoded), "Encoder must reject mismatched password");
    }

    @Test
    @DisplayName("Encoder rejects plaintext comparison without BCrypt hash (no plaintext fallback)")
    void matches_WithPlaintextAsEncodedHash_ReturnsFalse() {
        String plaintext = "unhashedPlaintextPassword";

        // BCryptPasswordEncoder.matches() should return false when compared against plaintext
        assertFalse(passwordEncoder.matches(plaintext, plaintext), "Plaintext must never match as an encoded hash");
    }

    @Test
    @DisplayName("Different encodings of the same password produce different hashes due to salt")
    void encode_ProducesDifferentSalts() {
        String password = "SamplePassword123";
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        assertNotEquals(hash1, hash2, "BCrypt must generate unique salts for each hash");
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }
}
