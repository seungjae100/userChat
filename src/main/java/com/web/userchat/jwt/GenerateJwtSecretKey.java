package com.web.userchat.jwt;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecretKey {

    public static void main(String[] args) {
        // SecureRandom 을 사용해 64바이트의 키 생성
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[64];
        secureRandom.nextBytes(key);

        // Base64 인코딩하여 SecretKey 생성
        String secretKey = Base64.getEncoder().encodeToString(key);

        // 결과 출력
        System.out.println("생성된 JWT key: " + secretKey);
    }
}
