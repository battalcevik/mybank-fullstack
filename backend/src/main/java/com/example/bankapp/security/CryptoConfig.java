package com.example.bankapp.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Value("${app.crypto.data-key-base64}")
    private String base64Key;

    private SecretKey dataKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) { // 256-bit key
            throw new IllegalStateException(
                    "app.crypto.data-key-base64 must be 32 bytes (256 bits) after Base64 decoding");
        }
        this.dataKey = new SecretKeySpec(keyBytes, "AES");
    }

    public SecretKey getDataKey() {
        return dataKey;
    }
}
