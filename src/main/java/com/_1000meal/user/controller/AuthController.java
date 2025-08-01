package com._1000meal.user.controller;

import com._1000meal.global.response.Result;
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
    public Result<String> loginSuccess(HttpSession session) {
        // 예: 세션에서 유저 정보를 꺼내거나, 인증 정보 확인
        return Result.ok(authService.getLoginSuccessMessage(session));
    }

    @GetMapping("/login/fail")
    public Result<String> loginFail() {
        // 상황에 맞게 메시지 세분화 가능
        return Result.ok("로그인에 실패했습니다. 아이디 또는 비밀번호를 다시 확인해주세요.");
    }

    @GetMapping("/logout")
    public Result<String> logout(HttpSession session) {
        authService.logout(session); // 세션 무효화, 추가 비즈니스 처리 등
        return Result.ok("로그아웃 되었습니다.");
    }
}