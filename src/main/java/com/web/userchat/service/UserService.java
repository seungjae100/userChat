package com.web.userchat.service;

import com.web.userchat.jwt.JwtProperties;
import com.web.userchat.jwt.JwtService;
import com.web.userchat.mapper.UserMapper;
import com.web.userchat.model.User;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import static com.web.userchat.util.CookieUtil.createCookie;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    // 회원가입
    public void register(User user) {
        // 이메일 중복 확인
        if (userMapper.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 데이터베이스에 user 정보 저장
        userMapper.save(user);
    }

    // 로그인 시 JWT 토큰을 쿠키에 설정하여 반환
    public Cookie[] login(String email, String password) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일이 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return generateTokenCookies(email);
    }

    // JWT 토큰을 생성하고 쿠키 배열로 반환하는 공통 메서드
    private Cookie[] generateTokenCookies(String email) {

        // AccessToken , RefreshToken 생성
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // 쿠키 생성
        Cookie accessTokenCookie = createCookie("accessToken", accessToken, (int)jwtProperties.getAccessTokenExpiration());
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, (int) jwtProperties.getRefreshTokenExpiration());

        // 쿠키 배열 반환
        return new Cookie[]{accessTokenCookie, refreshTokenCookie};
    }


    // 로그아웃 (RefreshToken 삭제)
    public void logout(String email) {
        jwtService.invalidateToken(email);
    }

    // Id 로 사용자 조회
    public User getUser(long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
