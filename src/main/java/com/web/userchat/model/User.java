package com.web.userchat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "사용자이름을 입력하세요.")
    private String username;

    @Email(message = "이메일 형식에 맞게 입력하세요")
    @NotBlank(message = "이메일을 입력하세요")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;


}
