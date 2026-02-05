package com._1000meal.adminlogin.service;

import com._1000meal.adminlogin.dto.AdminLoginResponse;
import com._1000meal.adminlogin.dto.AdminResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.model.AdminProfile;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.global.constant.Role;
import com._1000meal.global.error.code.AdminSignupErrorCode;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.security.JwtProvider;
import com._1000meal.global.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountAuthService {

    private final AccountRepository accountRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new CustomException(AdminSignupErrorCode.REQUIRED_FIELD_MISSING);
        }

        Account account = accountRepository.findByUserIdAndStatus(username, AccountStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS));

        if (account.getRole() != Role.ADMIN) {
            throw new CustomException(AdminSignupErrorCode.ADMIN_ALREADY_EXISTS);
        }

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_MISMATCH);
        }

        AdminProfile profile = adminProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_ACCESS_DENIED));

        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUserId(),
                profile.getDisplayName(),
                account.getEmail(),
                Role.ADMIN.name()
        );

        String token = jwtProvider.createToken(principal);

        return new AdminLoginResponse(
                token,
                account.getId(),
                account.getUserId(),
                profile.getDisplayName(),
                null
        );
    }

    @Transactional(readOnly = true)
    public AdminResponse getMyInfo(Long accountId) {
        if (accountId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Account account = accountRepository.findByIdAndStatusNot(accountId, AccountStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (account.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        AdminProfile profile = adminProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_ACCESS_DENIED));

        return new AdminResponse(
                account.getId(),
                account.getUserId(),
                profile.getDisplayName(),
                null
        );
    }

    @Transactional
    public void changePassword(Long accountId, String oldPassword, String newPassword) {
        if (accountId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (oldPassword == null || newPassword == null) {
            throw new CustomException(AdminSignupErrorCode.REQUIRED_FIELD_MISSING);
        }

        Account account = accountRepository.findByIdAndStatusNot(accountId, AccountStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (account.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_MISMATCH);
        }

        PasswordValidator.validatePassword(newPassword, account.getUserId(), null);
        account.changePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
