package com.web.userchat.controller;

import com.web.userchat.dto.LoginDTO;
import com.web.userchat.service.ChatService;
import com.web.userchat.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.web.userchat.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ChatService chatService;

    @GetMapping("/users/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/users/register")
    public String register(@Valid @ModelAttribute User user, HttpServletResponse response, Model model) {
        try {
            Cookie[] cookies = userService.register(user);
            for (Cookie cookie : cookies) {
                response.addCookie(cookie);
            }
            model.addAttribute("success", "회원가입이 완료되었습니다.");
            return "redirect:/users/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/users/login")
    public String loginForm(Model model) {
        model.addAttribute("LoginDTO", new LoginDTO());
        return "login";
    }

    @PostMapping("/users/login")
    public String login(@Valid @ModelAttribute LoginDTO loginDTO, HttpServletResponse response, Model model) {
        try {
            // UserService 에서 로그인 처리 후 쿠키 배열을 반환받음 (Access Token과 Refresh Token)
            Cookie[] cookies = userService.login(loginDTO.getEmail(), loginDTO.getPassword());
            // 모든 쿠키를 응답에 추가
            for (Cookie cookie : cookies) {
                response.addCookie(cookie);
            }

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(Principal principal) {
        if (principal != null) {
            String currentEmail = principal.getName(); // 현재 로그인한 사용자의 이메일을 가져온다.
            userService.logoutUser(currentEmail);
            chatService.logoutUser(currentEmail);
        }
        return "redirect:/";
    }
}
