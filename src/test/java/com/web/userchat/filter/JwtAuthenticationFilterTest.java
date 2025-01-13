package com.web.userchat.filter;

import com.web.userchat.jwt.JwtAuthenticationFilter;
import com.web.userchat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext(); // SecurityContext 초기화
    }

    @Test
    @DisplayName("유효한 토큰이 있는 요청 처리 테스트")
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        // Given
        String email = "test@example.com";
        String token = "valid.token.value";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", token)); // 토큰을 쿠키로 설정
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext에 인증 정보가 있어야 합니다.");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이 있는 요청 처리 테스트")
    void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        // Given
        String token = "invalid.token.value";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", token)); // 유효하지 않은 토큰을 쿠키로 설정
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext에 인증 정보가 없어야 합니다.");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 없는 요청 처리 테스트")
    void shouldNotAuthenticateWithoutToken() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext에 인증 정보가 없어야 합니다.");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}