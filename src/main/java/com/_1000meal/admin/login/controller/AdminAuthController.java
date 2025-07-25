package com._1000meal.admin.login.controller;

import com._1000meal.admin.login.dto.AdminLoginRequest;
import com._1000meal.admin.login.dto.AdminLoginResponse;
import com._1000meal.admin.login.entity.AdminEntity;
import com._1000meal.admin.login.security.JwtProvider;
import com._1000meal.admin.login.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminEntity admin = adminService.authenticate(request.getUsername(), request.getPassword());
        String token = jwtProvider.createToken(admin.getId(), admin.getUsername());
        return ResponseEntity.ok(new AdminLoginResponse(token));
    }
}