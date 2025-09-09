package com._1000meal.auth.controller;

import com._1000meal.auth.dto.FindIdRequest;
import com._1000meal.auth.dto.FindIdResponse;
import com._1000meal.auth.service.AccountService;
import com._1000meal.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@RequestBody @Valid FindIdRequest req) {
        return ResponseEntity.ok(accountService.findId(req));
    }
}