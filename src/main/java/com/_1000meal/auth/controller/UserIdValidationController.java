package com._1000meal.auth.controller;

import com._1000meal.auth.dto.UserIdValidateRequest;
import com._1000meal.auth.dto.UserIdValidateResponse;
import com._1000meal.auth.service.UserIdValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signup/user")
@RequiredArgsConstructor
public class UserIdValidationController {

    private final UserIdValidationService validationService;

    @PostMapping("/validate-id")
    public ResponseEntity<UserIdValidateResponse> validate(@RequestBody UserIdValidateRequest req) {
        UserIdValidateResponse res = validationService.validate(req.getUserId());
        return ResponseEntity.ok(res);
    }
}