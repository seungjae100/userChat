package com.web.userchat.jwt;

import com.web.userchat.mapper.TokenMapper;
import com.web.userchat.model.Token;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
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

    // AccessToken 을 위한 쿠키 생성 메서드
    public Cookie createAccessTokenCookie(String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true); // JavaScript 에서 접근 불가
        cookie.setSecure(false);  // HTTPS 만 사용 여부
        cookie.setPath("/");      // 쿠키 경로 설정
        cookie.setMaxAge((int) jwtProperties.getAccessTokenExpiration()); // 쿠키 만료 시간
        return cookie;
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

    public String getUserEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date getExpirationDate(long validityInSeconds) {
        return new Date(System.currentTimeMillis() + validityInSeconds * 1000);
    }
}
