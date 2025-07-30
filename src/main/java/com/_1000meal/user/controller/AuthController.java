package com._1000meal.user.controller;

import com._1000meal.user.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login/success")
    public String loginSuccess(HttpSession session) {
        return authService.getLoginSuccessMessage(session);
    }
}