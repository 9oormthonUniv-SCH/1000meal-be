package com._1000meal.auth.controller;



import com._1000meal.auth.dto.EmailChange.*;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.service.EmailChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account/email")
@RequiredArgsConstructor
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    @PostMapping("/start")
    public ResponseEntity<EmailChangeStartResponse> start(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeStartRequest req
    ) {
        return ResponseEntity.ok(emailChangeService.start(principal.id(), req));
    }

    @PostMapping("/code")
    public ResponseEntity<Map<String, String>> code(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeRequestCodeRequest req
    ) {
        emailChangeService.requestCode(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 새 이메일로 전송되었습니다."));
    }

    @PostMapping("/verify")
    public ResponseEntity<EmailChangeVerifyResponse> verify(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeVerifyRequest req
    ) {
        return ResponseEntity.ok(emailChangeService.verify(principal.id(), req));
    }
}