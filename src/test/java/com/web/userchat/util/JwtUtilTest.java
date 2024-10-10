package com.web.userchat.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    @DisplayName("토큰 생성 및 이메일 추출 테스트")
    void testGenerateAndExtractEmail() {
        // Given
        String email = "test@test.com";

        // When
        String token = jwtUtil.generateAccessToken(email);

        // Then
        assertNotNull(token, "토큰이 생성되어야 합니다.");
        assertEquals(email, jwtUtil.extractEmail(token), "토큰에서 추출한 이메일이 일치해야 합니다.");
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void testValidateToken() {
        // Given
        String email = "test@test.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid, "유효한 토큰이여야 합니다.");
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void testExpireToken() throws InterruptedException {
        // Given
        // 토큰 만료 시간을 짧게 설정해서 만료된 토큰 테스트
        String email = "test@test.com";
        JwtUtil shortLivedJwtUtil = new JwtUtil(secretKey, 1000, refreshTokenExpiration);
        String token = shortLivedJwtUtil.generateAccessToken(email);

        // When
        // 토큰이 만료될 때까지 대기 (1초 후 만료)
        Thread.sleep(2000);

        // Then
        assertFalse(shortLivedJwtUtil.validateToken(token, email), "토큰이 만료되어야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 테스트")
    void testInvalidToken() {
        // Given
        String invalidToken = "invalid.token.value";

        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtUtil.extractEmail(invalidToken);
        }, "유효하지 않은 토큰이여야 합니다.");
    }

    @Test
    @DisplayName("토큰 남은 유효시간 조회 테스트")
    void testGetExpireTime() {
        // Given
        String email = "test@test.com";
        String token = jwtUtil.generateAccessToken(email);
        // When
        Long expirationTime = jwtUtil.getExpireTime(token);
        // Then
        assertNotNull(expirationTime, "유효시간이 조회되어야 합니다.");
        assertTrue(expirationTime > 0, "유효시간은 양수여야 합니다.");
    }
}