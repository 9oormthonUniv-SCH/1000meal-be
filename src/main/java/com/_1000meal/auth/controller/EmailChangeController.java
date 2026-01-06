package com._1000meal.auth.controller;

import com._1000meal.auth.dto.EmailChange.*;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.service.EmailChangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(
        name = "Account - Email",
        description = "로그인된 사용자의 이메일 변경 절차 API (시작 → 인증코드 발송 → 검증)"
)
@RestController
@RequestMapping("/api/v1/account/email")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    @Operation(
            summary = "이메일 변경 절차 시작",
            description = """
                    이메일 변경 프로세스를 시작합니다.
                    
                    - 새로운 이메일을 입력받아 변경 요청을 생성합니다.
                    - 아직 이메일은 실제로 변경되지 않습니다.
                    - 이후 `/code`, `/verify` 단계를 순서대로 진행해야 합니다.
                    """
    )
    @PostMapping("/start")
    public ResponseEntity<EmailChangeStartResponse> start(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeStartRequest req
    ) {
        return ResponseEntity.ok(
                emailChangeService.start(principal.id(), req)
        );
    }

    @Operation(
            summary = "새 이메일로 인증 코드 발송",
            description = """
                    이메일 변경을 위해 새 이메일 주소로 인증 코드를 발송합니다.
                    
                    - `/start` API가 선행되어야 합니다.
                    - 일정 시간 내 재요청 시 제한(쿨타임)이 적용될 수 있습니다.
                    """
    )
    @PostMapping("/code")
    public ResponseEntity<Map<String, String>> code(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeRequestCodeRequest req
    ) {
        emailChangeService.requestCode(principal.id(), req);
        return ResponseEntity.ok(
                Map.of("message", "인증 코드가 새 이메일로 전송되었습니다.")
        );
    }

    @Operation(
            summary = "이메일 변경 인증 코드 검증",
            description = """
                    새 이메일로 전송된 인증 코드를 검증하고,
                    검증이 성공하면 계정의 이메일을 실제로 변경합니다.
                    
                    - 인증 코드가 유효하지 않거나 만료된 경우 실패합니다.
                    - 성공 시 변경된 이메일 정보가 반영됩니다.
                    """
    )
    @PostMapping("/verify")
    public ResponseEntity<EmailChangeVerifyResponse> verify(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid EmailChangeVerifyRequest req
    ) {
        return ResponseEntity.ok(
                emailChangeService.verify(principal.id(), req)
        );
    }
}