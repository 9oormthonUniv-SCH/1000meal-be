package com._1000meal.user.login.controller;

import com._1000meal.user.login.dto.UserLoginRequest;
import com._1000meal.user.login.dto.UserLoginResponse;
import com._1000meal.user.login.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService loginService;

    @PostMapping
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest req) {
        String token = loginService.login(req);
        return ResponseEntity.ok(new UserLoginResponse(token));
    }
}