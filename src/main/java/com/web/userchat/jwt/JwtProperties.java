package com.web.userchat.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secretKey; // JWT 시크릿 키
    private long accessTokenExpiration; // AccessToken 만료시간
    private long refreshTokenExpiration; // RefreshToken 만료시간



}
