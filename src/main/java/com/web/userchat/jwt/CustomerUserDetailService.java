package com.web.userchat.jwt;

import com.web.userchat.mapper.UserMapper;

import com.web.userchat.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailService implements UserDetailsService {

    private final UserMapper userMapper;

    public CustomerUserDetailService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            User user = userMapper.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. : " + email));

        // Spring Security 의 User 객체 생성 , 보안을 위해 최소한의 정보만
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}

