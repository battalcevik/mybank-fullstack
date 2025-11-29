package com.example.bankapp.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA converter that encrypts/decrypts String fields using CryptoService.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    // static reference because JPA instantiates the converter itself
    private static CryptoService staticCryptoService;

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        EncryptedStringConverter.staticCryptoService = cryptoService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || staticCryptoService == null) {
            return attribute;
        }
        return staticCryptoService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || staticCryptoService == null) {
            return dbData;
        }
        return staticCryptoService.decrypt(dbData);
    }
}
