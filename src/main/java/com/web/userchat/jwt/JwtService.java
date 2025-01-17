package com.web.userchat.jwt;

import com.web.userchat.mapper.TokenMapper;
import com.web.userchat.model.Token;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final TokenMapper tokenMapper;

    // AccessToken 생성 메서드
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email) // 토큰 제목으로 이메일 설정
                .setIssuedAt(new Date()) // 토큰 발행 시간
                .setExpiration(getExpirationDate(jwtProperties.getAccessTokenExpiration()))
                .signWith(getSigningKey()) // 서명 키로 서명
                .compact(); // 토큰 생성
    }

    // RefreshToken 생성 및 저장
    public String generateRefreshToken(String email) {
        String refreshToken = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(getExpirationDate(jwtProperties.getRefreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        // DB 에 토큰 저장
        saveRefreshToken(refreshToken, email);
        return refreshToken;
    }

    // 토큰 삭제
    public void invalidateToken(String email) {
        tokenMapper.deleteRefreshToken(email);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 토큰에서 사용자 이메일 추출
    public String getUserEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // RefreshToken 저장
    private void saveRefreshToken(String refreshToken, String email) {
        Token token = new Token();
        token.setRefreshToken(refreshToken);
        token.setEmail(email);
        token.setCreateDate(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiration()));

        tokenMapper.deleteRefreshToken(email);
        tokenMapper.saveRefreshToken(token);
    }
    // JWT 비밀키 생성
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
    // 만료 시간
    private Date getExpirationDate(long validityInSeconds) {
        return new Date(System.currentTimeMillis() + validityInSeconds * 1000);
    }



}
