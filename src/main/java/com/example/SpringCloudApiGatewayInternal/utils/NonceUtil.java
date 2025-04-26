package com.example.SpringCloudApiGatewayInternal.utils;

import java.security.SecureRandom;
import java.util.Random;

public class NonceUtil {
    private static final String SECRET_KEY = "nonce-secret-key"; //

    public static String generateNonce(String sessionId) {
        try {
            String dataToEncrypt =  sessionId +"-"+ System.currentTimeMillis()+  "-"+ genSecureRandNumber(5);
            return DataUtils.encrypt(SECRET_KEY, dataToEncrypt,"AES");
        } catch (Exception e) {
            throw new RuntimeException("Error generating nonce", e);
        }
    }

    public static String decryptNonce(String nonce) {
        try {
            return DataUtils.decrypt(SECRET_KEY, nonce,"AES");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting nonce", e);
        }
    }
    public static int genRandNumber(int bound) {
        Random random = new Random();
        int lowerBound = (int) Math.pow(10, bound - 1);
        int upperBound = (int) Math.pow(10, bound) - 1;
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }
    public static int genSecureRandNumber(int bound) {
        SecureRandom random = new SecureRandom();
        int lowerBound = (int) Math.pow(10, bound - 1);
        int upperBound = (int) Math.pow(10, bound) - 1;
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }

}