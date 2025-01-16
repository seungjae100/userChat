package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Token {

    private Long id; // 토큰 고유 아이디

    private String refreshToken; // 리프레시 토큰

    private String email; // 이메일

    private LocalDateTime createDate;
    private LocalDateTime expiryDate; // 토큰 만료시간
}
