package com._1000meal.auth.service;

import com._1000meal.auth.dto.ChangePasswordRequest;
import com._1000meal.auth.dto.DeleteAccountRequest;
import com._1000meal.auth.dto.FindIdRequest;
import com._1000meal.auth.dto.FindIdResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.global.constant.Role;
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

    /* 아이디(학번) 찾기: 이메일 + 이름 일치 */
    @Transactional(readOnly = true)
    public FindIdResponse findId(FindIdRequest req) {
        // 1) 이메일로 계정 찾기
        Account account = accountRepository.findByEmail(req.email())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 이메일(" + req.email() + ")로 가입된 계정을 찾을 수 없습니다.")
                );

        // 2) 프로필에서 이름 확인
        var profile = userProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "계정은 존재하지만 연결된 프로필 정보를 찾을 수 없습니다.")
                );

        // 3) 이름 일치 검사 (대소문자 무시하려면 equalsIgnoreCase로 교체)
        String inputName = req.name() == null ? "" : req.name().trim();
        String savedName = profile.getName() == null ? "" : profile.getName().trim();

        if (!savedName.equals(inputName)) {
            throw new CustomException(
                    ErrorCode.VALIDATION_ERROR,
                    "입력한 이름(" + inputName + ")이 계정에 등록된 이름과 일치하지 않습니다."
            );
        }

        // 4) 통과 → 학번(아이디) 반환
        return FindIdResponse.of(account.getUserId());
    }

    /* 비밀번호 변경 (로그인 상태 - accountId 보유) */
    @Transactional
    public void changePassword(Long accountId, ChangePasswordRequest req) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 ID(" + accountId + ")의 계정을 찾을 수 없습니다.")
                );

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(req.currentPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED,
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        // 새 비밀번호 확인
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."
            );
        }

        // 정책 검증
        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);

        // 기존과 동일 금지
        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호가 이전 비밀번호와 동일합니다."
            );
        }

        // 변경
        account.changePassword(passwordEncoder.encode(req.newPassword()));
    }

    /* 비밀번호 변경 (동일 목적: 명시적 메서드) */
    @Transactional
    public void changePasswordByAccountId(Long accountId, ChangePasswordRequest req) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 ID(" + accountId + ")의 계정을 찾을 수 없습니다.")
                );

        if (!passwordEncoder.matches(req.currentPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED,
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."
            );
        }

        PasswordValidator.validatePassword(req.newPassword(), account.getUserId(), null);

        if (passwordEncoder.matches(req.newPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "새 비밀번호가 이전 비밀번호와 동일합니다."
            );
        }

        account.changePassword(passwordEncoder.encode(req.newPassword()));
    }

    /* 회원 탈퇴 (로그인 상태) */
    @Transactional
    public void deleteOwnAccountByAccountId(Long accountId, DeleteAccountRequest req) {
        if (Boolean.FALSE.equals(req.agree())) {
            throw new CustomException(
                    ErrorCode.PRECONDITION_REQUIRED,
                    "회원 탈퇴 동의가 필요합니다."
            );
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        "탈퇴하려는 계정을 찾을 수 없습니다. accountId=" + accountId)
                );

        if (account.getRole() == Role.ADMIN) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "관리자 계정은 스스로 탈퇴할 수 없습니다. 운영팀에 문의하세요."
            );
        }

        if (!passwordEncoder.matches(req.currentPassword(), account.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED,
                    "현재 비밀번호가 올바르지 않아 탈퇴를 진행할 수 없습니다."
            );
        }

        if (account.getStatus() == AccountStatus.DELETED) {
            return; // 멱등성 보장
        }

        account.deleteAndReleaseIdentifiers();
    }
}