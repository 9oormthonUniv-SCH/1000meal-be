package com._1000meal.auth.service;
import com._1000meal.auth.dto.FindIdRequest;
import com._1000meal.auth.dto.FindIdResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public FindIdResponse findId(FindIdRequest req) {
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return FindIdResponse.of(account.getUserId());
    }
}