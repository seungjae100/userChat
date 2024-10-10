package com.web.userchat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // 테스트 전 데이터 삭제
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUser_success() throws Exception {
        // Given
        User user = new User();
        user.setUsername("tester");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");

        // When
        var resultActions = mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", user.getUsername())
                .param("email", user.getEmail())
                .param("password", user.getPassword())
                .with(SecurityMockMvcRequestPostProcessors.csrf())); // CSRF 토큰 추가

        // Then
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/users/login"));

        // 쿠키의 유효성을 검사하기 위해 응답을 추출합니다.
        var response = resultActions.andReturn().getResponse();

        // accessToken 쿠키 유효성 검사
        Cookie accessTokenCookie = response.getCookie("accessToken");
        assertNotNull(accessTokenCookie, "AccessToken 쿠키가 존재해야 합니다.");
        assertTrue(accessTokenCookie.isHttpOnly(), "AccessToken 쿠키는 HttpOnly 설정이 되어 있어야 합니다.");
        assertEquals("/", accessTokenCookie.getPath(), "AccessToken 쿠키의 경로는 '/' 이어야 합니다.");

        // refreshToken 쿠키 유효성 검사
        Cookie refreshTokenCookie = response.getCookie("refreshToken");
        assertNotNull(refreshTokenCookie, "RefreshToken 쿠키가 존재해야 합니다.");
        assertTrue(refreshTokenCookie.isHttpOnly(), "RefreshToken 쿠키는 HttpOnly 설정이 되어 있어야 합니다.");
        assertEquals("/", refreshTokenCookie.getPath(), "RefreshToken 쿠키의 경로는 '/' 이어야 합니다.");
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복 이메일")
    void registerUser_fail_email() throws Exception {
        // Given
        User user = new User();
        user.setUsername("tester");
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        userRepository.save(user); // 중복 이메일 사용자 저장

        User user2 = new User();
        user2.setUsername("tester2");
        user2.setEmail("test@gmail.com");
        user2.setPassword("123456");

        // When & Then
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", user2.getUsername())
                        .param("email", user2.getEmail())
                        .param("password", user2.getPassword())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk()) // 실패 시, 다시 폼으로 돌아오기 때문에 200 OK
                .andExpect(model().attributeExists("error")) // 에러 메시지가 모델에 포함되어 있는지 확인
                .andExpect(model().attribute("error", "이미 사용중인 이메일입니다."));
    }
}