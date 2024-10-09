package com.web.userchat.service;

import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(User user) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "이미 사용중인 이메일입니다.";
        }

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 사용자 저장
        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }
}
