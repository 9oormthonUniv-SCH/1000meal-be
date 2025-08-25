package com._1000meal.email.controller;

import com._1000meal.email.dto.EmailSendRequest;
import com._1000meal.email.dto.EmailStatusResponse;
import com._1000meal.email.dto.EmailVerifyRequest;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@RequestBody EmailSendRequest request) {
        emailService.issueAndStoreCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, SuccessCode.OK));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@RequestBody EmailVerifyRequest req) {
        emailService.verifyCode(req.getEmail(), req.getCode());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", SuccessCode.OK));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<EmailStatusResponse>> status(@RequestParam String email) {
        boolean verified = emailService.isEmailVerified(email);
        return ResponseEntity.ok(ApiResponse.ok(new EmailStatusResponse(email, verified)));
    }
}

