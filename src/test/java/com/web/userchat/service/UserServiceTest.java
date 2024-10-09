package com.web.userchat.service;

import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        String result = userService.register(user);

        // Then
        assertEquals("회원가입이 완료되었습니다.", result);
        assertTrue(userRepository.findByEmail("test@gmail.com").isPresent());

        User savedUser = userRepository.findByEmail("test@gmail.com").get();
        assertTrue(passwordEncoder.matches("123456",savedUser.getPassword()));
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

        // When
        String result = userService.register(user2);

        // Then
        assertEquals("이미 사용중인 이메일입니다.", result);
    }
}