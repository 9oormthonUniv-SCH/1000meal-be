package com._1000meal.email.controller;

import com._1000meal.email.dto.EmailSendRequest;
import com._1000meal.email.dto.EmailVerifyRequest;
import com._1000meal.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signup/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailSendRequest request) {
        String email = request.getEmail();
        if (!email.endsWith("@sch.ac.kr")) {
            return ResponseEntity.badRequest().body("순천향대학교 이메일만 인증할 수 있습니다.");
        }
        emailService.issueAndStoreCode(email);  // ← 이것만
        return ResponseEntity.ok("이메일이 전송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody EmailVerifyRequest req) {
        emailService.verifyCode(req.getEmail(), req.getCode());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    // 선택: 현재 인증상태 조회
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String email) {
        return ResponseEntity.ok(emailService.isEmailVerified(email));
    }
}