package com._1000meal.user.controller;

import com._1000meal.global.response.ApiResponse;
import com._1000meal.global.error.code.SuccessCode;
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
    public ApiResponse<String> loginSuccess(HttpSession session) {
        return ApiResponse.ok(authService.getLoginSuccessMessage(session)); // 200 OK
    }

    @GetMapping("/login/fail")
    public ApiResponse<String> loginFail() {
        return ApiResponse.success("로그인에 실패했습니다. 아이디 또는 비밀번호를 다시 확인해주세요.", SuccessCode.OK); // 200 OK (원하면 custom 코드도 추가)
    }

    @GetMapping("/logout")
    public ApiResponse<String> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.success("로그아웃 되었습니다.", SuccessCode.OK); // 200 OK
    }
}