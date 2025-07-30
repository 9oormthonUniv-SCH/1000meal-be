package com._1000meal.user.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/login/success")
    public String loginSuccess(HttpSession session) {
        // 세션에서 사용자 정보 꺼내기
        Object user = session.getAttribute("user");
        if (user != null) {
            return "로그인 성공! " + user.toString();
        } else {
            return "로그인 성공했지만 사용자 정보가 없습니다.";
        }
    }
}