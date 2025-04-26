package com.example.SpringCloudApiGatewayInternal.utils;

import java.security.SecureRandom;

public class SessionUtil {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz012345";
    private static final int SESSION_ID_LENGTH = 24;

    public static String generateSessionID(){
        SecureRandom random = new SecureRandom();
        StringBuilder sessionIdBuilder = new StringBuilder(SESSION_ID_LENGTH);
        for (int i = 0; i < SESSION_ID_LENGTH; i++) {
            sessionIdBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sessionIdBuilder.toString();
    }
}
