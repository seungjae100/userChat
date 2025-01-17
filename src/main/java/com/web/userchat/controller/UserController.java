package com.web.userchat.controller;

import com.web.userchat.model.User;
import com.web.userchat.service.UserService;
import com.web.userchat.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user){
        userService.register(user);
        return "회원가입이 완료되었습니다.";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        Cookie[] cookies = userService.login(email, password);
        CookieUtil.addCookie(response, cookies);
        return "로그인 성공";
    }

    @DeleteMapping("/logout")
    public String logout(@RequestParam String email) {
        userService.logout(email);
        return "로그아웃 되었습니다.";
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id){
        return userService.getUser(id);
    }
}
