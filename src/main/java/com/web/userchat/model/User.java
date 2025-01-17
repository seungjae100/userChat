package com.web.userchat.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {

    private Long id; // 사용자 고유 아이디 (PK)

    private String username; // 사용자 이름

    private String email; // 사용자 이메일

    private String password; // 사용자 비밀번호

    private int likeCount; // 사용자 좋아요 수


}
