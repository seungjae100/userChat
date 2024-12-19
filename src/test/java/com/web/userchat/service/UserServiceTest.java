package com.web.userchat.service;

import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void register_Success() {
        // Given
        User user = new User();
        user.setUsername("test");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");

        // When
        Cookie[] cookies = userService.register(user);

        // Then
        assertNotNull(cookies, "쿠키가 반환되어야 합니다.");
        assertEquals(2, cookies.length, "Access 토큰과 Refresh 토큰 두 개의 쿠키가 있어야 합니다.");
        assertTrue(userRepository.findByEmail("test@gmail.com").isPresent(), "회원가입 후 유저가 저장되어야 합니다.");

        User savedUser = userRepository.findByEmail("test@gmail.com").get();
        assertTrue(passwordEncoder.matches("123456", savedUser.getPassword()), "비밀번호가 암호화되어 저장되어야 합니다.");

        String accessToken = jwtUtil.extractEmail(cookies[0].getValue());
        assertEquals("test@gmail.com", accessToken, "Access 토큰에서 추출한 이메일이 저장된 사용자 이메일과 일치해야 합니다.");
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복 이메일")
    void register_Fail() {
        // Given
        User user = new User();
        user.setUsername("tester");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        userRepository.save(user);

        User user2 = new User();
        user2.setUsername("tester2");
        user2.setEmail("test@gmail.com");
        user2.setPassword("123456");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(user2);
        }, "중복된 이메일로 회원가입 시 예외가 발생해야 합니다.");

        assertEquals("이미 사용중인 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // Given
        User user = new User();
        user.setUsername("test");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        userService.register(user); // 회원가입

        // When
        Cookie[] cookies = userService.login("test@gmail.com", "123456");

        Cookie accessTokenCookie = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                        .findFirst().orElse(null);

        // Then
        assertNotNull(accessTokenCookie, "로그인 시 Access 토큰 쿠키가 반환되어야 합니다.");
        assertEquals("accessToken", accessTokenCookie.getName(), "쿠키의 이름은 accessToken이여야 합니다.");
        assertTrue(jwtUtil.validateToken(accessTokenCookie.getValue(), "test@gmail.com"), "생성된 토큰은 유효해야 합니다.");
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() {
        // Given
        User user = new User();
        user.setUsername("test");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        userService.register(user); // 회원가입

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("test@gmail.com", "wrongPassword");
        }, "잘못된 비밀번호로 로그인 시 예외가 발생해야 합니다.");

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }
}