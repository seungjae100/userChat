package com.web.userchat.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    public static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // JavaScript 접근 금지
        cookie.setSecure(false);  // HTTPS만 사용 여부
        cookie.setPath("/");      // 전체 경로에서 쿠키 사용
        cookie.setMaxAge(maxAge); // 쿠키 만료 시간
        return cookie;
    }
    //
    public static void addCookie(HttpServletResponse response, Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            response.addCookie(cookie);
        }
    }
}
