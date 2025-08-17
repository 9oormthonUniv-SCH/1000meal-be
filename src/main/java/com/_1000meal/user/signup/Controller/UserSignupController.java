package com._1000meal.user.signup.Controller;

import com._1000meal.user.signup.dto.UserSignupRequest;
import com._1000meal.user.signup.dto.UserSignupResponse;
import com._1000meal.user.signup.service.UserSignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signup/user")
@RequiredArgsConstructor
public class UserSignupController {

    private final UserSignupService signupService;

    @PostMapping
    public ResponseEntity<UserSignupResponse> signup(@RequestBody UserSignupRequest req) {
        return ResponseEntity.ok(signupService.signup(req));
    }
}