package com._1000meal.auth.service;
import com._1000meal.auth.dto.ChangePasswordRequest;
import com._1000meal.auth.dto.FindIdRequest;
import com._1000meal.auth.dto.FindIdResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public FindIdResponse findId(FindIdRequest req) {
        // 1) 이메일로 계정 찾기
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2) 프로필에서 이름 확인
        var profile = userProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3) 이름 일치 검사 (공백/대소문자 대응은 팀 규칙에 맞춰 선택)
        String inputName = req.name().trim();
        String savedName = profile.getName() == null ? "" : profile.getName().trim();

        if (!savedName.equals(inputName)) { // 대소문자 구분 없이 하려면 equalsIgnoreCase 사용
            throw new CustomException(ErrorCode.USER_NOT_FOUND); // 또는 VALIDATION_ERROR
        }

        // 4) 통과 → 학번(아이디) 반환
        return FindIdResponse.of(account.getUserId());
    }

    @Transactional
    public void changePassword(Long accountId, ChangePasswordRequest req) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(req.currentPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 새 비밀번호 확인
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);

        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 비밀번호 변경
        account.changePassword(passwordEncoder.encode(req.newPassword()));
    }


    @Transactional
    public void changePasswordByAccountId(Long accountId, ChangePasswordRequest req) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.currentPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED); // "현재 비밀번호가 일치하지 않습니다." 등으로 세분화 가능
        }
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);
        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        account.changePassword(passwordEncoder.encode(req.newPassword()));
    }
}