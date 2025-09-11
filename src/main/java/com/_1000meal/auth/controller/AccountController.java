package com._1000meal.auth.controller;

import com._1000meal.auth.dto.*;
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
    /** 로그인 상태 회원 탈퇴 (소프트 삭제 + 식별자 반환) */
    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        accountService.deleteOwnAccountByAccountId(principal.id()); // 바디 제거
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    /** 1) 새 이메일로 인증코드 발송 (로그인 필요) */
    @PostMapping("/email/change/request")
    public ResponseEntity<Map<String, String>> requestChangeEmail(
            @AuthenticationPrincipal com._1000meal.auth.model.AuthPrincipal principal,
            @RequestBody @Valid ChangeEmailRequest req
    ) {
        accountService.requestChangeEmail(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 새 이메일로 전송되었습니다."));
    }

    /** 2) 인증코드 확인 후 실제 이메일 변경 */
    @PostMapping("/email/change/confirm")
    public ResponseEntity<Map<String, String>> confirmChangeEmail(
            @AuthenticationPrincipal com._1000meal.auth.model.AuthPrincipal principal,
            @RequestBody @Valid ChangeEmailConfirmRequest req
    ) {
        accountService.confirmChangeEmail(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "이메일이 변경되었습니다."));
    }

}