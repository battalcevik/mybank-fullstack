package com.example.bankapp.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;    // 96 bits, recommended for GCM
    private static final int TAG_LENGTH_BITS = 128;   // 16-byte auth tag

    private final SecretKey dataKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(CryptoConfig cryptoConfig) {
        this.dataKey = cryptoConfig.getDataKey();
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            // 1) Generate random IV
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            // 2) Init cipher in GCM mode
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, dataKey, spec);

            // 3) Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 4) Combine IV + ciphertext+tag into single byte[]
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            // 5) Return Base64 string for storage in DB
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Error encrypting data", e);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            if (combined.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            // 1) Split IV and ciphertext+tag
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];

            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            // 2) Init cipher in decrypt mode
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, dataKey, spec);

            // 3) Decrypt
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error decrypting data", e);
        }
    }
}
