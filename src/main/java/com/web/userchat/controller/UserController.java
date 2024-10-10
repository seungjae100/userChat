package com.web.userchat.controller;

import com.web.userchat.dto.LoginDTO;
import com.web.userchat.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("LoginDTO", new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response, Model model) {
        try {
            Cookie accessTokenCookie = userService.login(loginDTO.getEmail(), loginDTO.getPassword());
            response.addCookie(accessTokenCookie);
            return "redirect:/users/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
}
