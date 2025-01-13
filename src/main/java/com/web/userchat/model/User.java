package com.web.userchat.model;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class User {

    private Long id; // User 고유 아이디

    private String username; // 사용자 이름

    private String email; // 사용자 이메일

    private String password; // 사용자 비밀번호
}
