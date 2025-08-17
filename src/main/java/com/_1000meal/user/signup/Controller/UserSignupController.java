package com._1000meal.user.signup.Controller;

import com._1000meal.user.signup.dto.UserSignupRequest;
import com._1000meal.user.signup.service.UserSignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signup/user")
@RequiredArgsConstructor
public class UserSignupController {

    private final UserSignupService signupService;

    @PostMapping
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest req) {
        Long id = signupService.signup(req);
        return ResponseEntity.ok(id);
    }
}