package com._1000meal.email.controller;

import com._1000meal.email.dto.EmailSendRequest;
import com._1000meal.email.dto.EmailStatusResponse;
import com._1000meal.email.dto.EmailVerifyRequest;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
@Tag(
        name = "Email Authentication",
        description = "이메일 인증 코드 발송, 인증 확인, 인증 상태 조회 API"
)
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(
            summary = "이메일 인증 코드 발송",
            description = """
            학교 이메일(@sch.ac.kr)로 인증 코드를 발송합니다.

            - 이미 인증되지 않은 기존 인증 요청이 있다면 무효화됩니다.
            - 인증 코드는 제한 시간 내에만 유효합니다.
            - 인증 메일은 트랜잭션 커밋 이후 발송됩니다.
            """
    )
    public ResponseEntity<ApiResponse<Void>> sendEmail(@RequestBody EmailSendRequest request) {
        emailService.issueAndStoreCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, SuccessCode.OK));
    }

    @PostMapping("/verify")
    @Operation(
            summary = "이메일 인증 코드 검증",
            description = """
            사용자가 입력한 인증 코드를 검증하여 이메일 인증을 완료합니다.

            - 인증 코드가 일치하지 않거나 만료된 경우 실패합니다.
            - 성공 시 해당 이메일은 '인증 완료' 상태가 됩니다.
            """
    )
    public ResponseEntity<ApiResponse<String>> verify(@RequestBody EmailVerifyRequest req) {
        emailService.verifyCode(req.getEmail(), req.getCode());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", SuccessCode.OK));
    }

    @GetMapping("/status")
    @Operation(
            summary = "이메일 인증 상태 조회",
            description = """
            특정 이메일의 인증 완료 여부를 조회합니다.

            - 회원가입/로그인 시 인증 여부를 확인하는 용도로 사용됩니다.
            """
    )
    public ResponseEntity<ApiResponse<EmailStatusResponse>> status(@RequestParam String email) {
        boolean verified = emailService.isEmailVerified(email);
        return ResponseEntity.ok(ApiResponse.ok(new EmailStatusResponse(email, verified)));
    }
}