package com._1000meal.auth.service;


import com._1000meal.auth.dto.UserIdValidateResponse;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
// com._1000meal.auth.service.UserIdValidationService
@Service
@RequiredArgsConstructor
public class UserIdValidationService {

    private final AccountRepository accountRepository;

    public UserIdValidateResponse validate(String raw) {
        final String userId = raw == null ? "" : raw.trim();

        // 1) 형식 검증
        if (!userId.matches("^\\d{8}$")) {
            return new UserIdValidateResponse(false, "INVALID_FORMAT", "학번(8자리) 형식이 아닙니다.");
        }

        // 2) 중복 검증 - 반드시 existsByUserId 사용!
        boolean exists = accountRepository.existsByUserId(userId);
        if (exists) {
            return new UserIdValidateResponse(false, "DUPLICATED", "이미 사용 중인 학번입니다.");
        }

        return new UserIdValidateResponse(true, "AVAILABLE", "사용 가능한 학번입니다.");
    }
}