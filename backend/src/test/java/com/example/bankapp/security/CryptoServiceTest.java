package com.example.bankapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    void encryptAndDecryptShouldRoundTrip() {
        String original = "Sensitive PII 1234567";
        String encrypted = cryptoService.encrypt(original);

        assertNotNull(encrypted, "Encrypted value should not be null");
        assertNotEquals(original, encrypted, "Ciphertext should not equal plaintext");

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(original, decrypted, "Decrypted value should equal original plaintext");
    }

    @Test
    void samePlaintextShouldProduceDifferentCiphertexts() {
        String original = "Same value here";

        String encrypted1 = cryptoService.encrypt(original);
        String encrypted2 = cryptoService.encrypt(original);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2,
                "Encrypting the same plaintext twice should give different ciphertexts (random IV)");
    }
}
