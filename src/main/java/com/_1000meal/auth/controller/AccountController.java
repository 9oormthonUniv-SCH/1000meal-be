package com._1000meal.auth.controller;

import com._1000meal.auth.dto.ChangePasswordRequest;
import com._1000meal.auth.dto.FindIdRequest;
import com._1000meal.auth.dto.FindIdResponse;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.service.AccountService;
import com._1000meal.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@RequestBody @Valid FindIdRequest req) {
        return ResponseEntity.ok(accountService.findId(req));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal com._1000meal.auth.model.AuthPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest req
    ) {
        // record 접근자는 getId()가 아니라 id()
        accountService.changePasswordByAccountId(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}