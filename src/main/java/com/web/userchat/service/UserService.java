package com.web.userchat.service;

import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 회원가입 시 JWT 토큰을 쿠기에 설정하여 반환
    public Cookie[] register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return generateTokenCookies(user.getEmail());
    }

    // 로그인 시 JWT 토큰을 쿠키에 설정하여 반환
    public Cookie[] login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return generateTokenCookies(user.getEmail());
    }

    // JWT 토큰을 생성하고 쿠키 배열로 반환하는 공통 메서드
    private Cookie[] generateTokenCookies(String email) {
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        Cookie accessTokenCookie = createCookie("accessToken", accessToken, 60 * 60);
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60);

        return new Cookie[]{accessTokenCookie, refreshTokenCookie};
    }

    // 쿠키를 생성하는 메서드
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false);
        cookie.setSecure(false); // HTTP 테스트 시 false, HTTPS 환경에서는 true 로 설정
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
