package com.web.userchat.controller;

import com.web.userchat.service.UserService;
import jakarta.validation.Valid;
import com.web.userchat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid User user, Model model) {
        String result = userService.register(user);

        if (result.equals("이미 사용중인 이메일입니다.")) {
            model.addAttribute("error", result);
            return "register"; // 회원가입 폼으로 다시 돌아가면서 에러 메시지 표시
        }

        model.addAttribute("success", "회원가입이 완료되었습니다.");
        return "redirect:/users/login"; // 회원가입 후 로그인 페이지로 리다이렉트
    }
}
