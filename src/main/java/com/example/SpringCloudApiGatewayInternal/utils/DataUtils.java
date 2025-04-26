package com.example.SpringCloudApiGatewayInternal.utils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class DataUtils {
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty() || value.isBlank();
    }

    public static String removeHeaderKey(String key) {
        if (isNullOrEmpty(key)) return null;
        if (key.contains(Const.KEY_PAIR.PUBLIC_KEY_HEADER) && key.contains(Const.KEY_PAIR.PUBLIC_KEY_FOOTER)) {
            key = key
                    .replace(Const.KEY_PAIR.PUBLIC_KEY_HEADER, "")
                    .replace(Const.KEY_PAIR.PUBLIC_KEY_FOOTER, "")
                    .replace("\n", "")
                    .trim();
        } else if (key.contains(Const.KEY_PAIR.PRIVATE_KEY_HEADER) && key.contains(Const.KEY_PAIR.PRIVATE_KEY_FOOTER)) {
            key = key
                    .replace(Const.KEY_PAIR.PRIVATE_KEY_HEADER, "")
                    .replace(Const.KEY_PAIR.PRIVATE_KEY_FOOTER, "")
                    .replace("\n", "")
                    .trim();
        }
        key = key.replace("\r", "");
        return key;
    }

    public static String generateMD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception ex) {
            return null;
        }
    }

    public static String generateSignature(String secretKey, String stringToSign) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception ex) {
            return null;
        }
    }
    public static boolean verifySignature(String secretKey, String stringToSign, String expectedSignature) {
        String computedSignature = generateSignature(secretKey, stringToSign);
        return computedSignature != null && computedSignature.equals(expectedSignature);
    }

    public static String encrypt(String secretKey, String data,String ALGORITHM) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Decrypt the data
    public static String decrypt(String secretKey, String encryptedData, String algorithm) throws Exception {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data: " + e.getMessage(), e);
        }
    }

}
