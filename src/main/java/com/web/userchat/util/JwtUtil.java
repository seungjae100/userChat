package com.web.userchat.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpirationTime;  // Access 토큰 만료 시간
    private final long refreshTokenExpirationTime; // Refresh 토큰 만료 시간

    // 생성자에서 환경 변수로부터 설정을 읽어온다.
    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token.expiration}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpirationTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // Access 토큰 생성
    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenExpirationTime);
    }

    // Refresh 토큰 생성
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenExpirationTime);
    }

    // 토큰 생성 기본 메서드
    private String generateToken(String email, long expirationTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS512) // 서명 알고리즘을 명시적으로 설정
                .compact();
    }

    // 토큰에서 이메일 추출
    public String extractEmail(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (JwtException e) {
            throw new JwtException("유효하지 않은 토큰입니다.", e);
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token, String email) {
        try {
            Claims claims = getClaims(token);
            boolean isEmailValid = email.equals(claims.getSubject());
            boolean isTokenExpired = claims.getExpiration().before(new Date());
            return isEmailValid && !isTokenExpired;
        } catch (JwtException e) {
            return false; // 토큰 파싱 실패 시 false 반환
        }
    }

    // 토큰 남은 유효시간 조회
    public Long getExpireTime(String token) {
        Date expiration = getClaims(token).getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    // Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
