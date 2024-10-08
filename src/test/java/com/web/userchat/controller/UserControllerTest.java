package com.web.userchat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        // When & Then

        mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user))
                    .with(SecurityMockMvcRequestPostProcessors.csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));

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
        mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user2))
                    .with(SecurityMockMvcRequestPostProcessors.csrf())) // CSRF 토큰 추가
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 사용중인 이메일입니다."));

    }
}
