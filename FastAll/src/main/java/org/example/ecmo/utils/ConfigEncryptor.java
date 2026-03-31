package org.example.ecmo.utils;

import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class ConfigEncryptor {

    @Value("${config.encrypt.key:default-key-please-change}")
    private String encryptKey;

    private AES aes;

    @PostConstruct
    public void init() {
        byte[] key = new byte[16];
        byte[] keyBytes = encryptKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 16));
        this.aes = new AES(key);
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        return aes.encryptHex(plainText);
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        return aes.decryptStr(encryptedText);
    }
}
