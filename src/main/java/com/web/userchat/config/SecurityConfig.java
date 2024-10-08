package com.web.userchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
    * 비밀번호 암호화
    * : 회원가입 시 비밀번호를 BCryptPasswordEncoder 를 사용하여 암호화한 후 데이터베이스에 저장합니다.
    */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .requestMatchers("/api/users/register").permitAll() // 회원가입 엔드포인트는 인증 없이 허용
                .anyRequest().authenticated(); // 나머지 요청은 인증 필요

        return http.build();
    }
}
