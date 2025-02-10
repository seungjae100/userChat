package com.web.userchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @Email(message = "이메일 형식에 맞게 입력하세요")
    @NotBlank(message = "이메일을 입력하세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;

    public LoginDTO() {

    }

    public LoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
