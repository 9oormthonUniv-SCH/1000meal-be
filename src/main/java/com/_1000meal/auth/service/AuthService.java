package com._1000meal.auth.service;

import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.auth.dto.SignupResponse;
import com._1000meal.auth.model.*;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.email.service.EmailService;
import com._1000meal.global.constant.Role;
import com._1000meal.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepo;
    private final UserProfileRepository userProfileRepo;
    private final AdminProfileRepository adminProfileRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        // 0) 입력 정규화
        if (req.role() == null) throw new IllegalArgumentException("역할(role)은 필수입니다.");

        final String userId = req.userId().trim();
        final String email  = req.email().trim().toLowerCase();
        final Role role     = req.role();

        // 1) 유니크 검사
        if (accountRepo.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (accountRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2) 역할별 정책
        AccountStatus status = AccountStatus.ACTIVE;
        if (role == Role.STUDENT) {
            // 학번 형식: 8자리 숫자
            if (!userId.matches("^\\d{8}$")) {
                throw new IllegalArgumentException("학번 형식이 올바르지 않습니다.");
            }
            // 이메일 도메인
            if (!email.endsWith("@sch.ac.kr")) {
                throw new IllegalArgumentException("학생 계정은 @sch.ac.kr 이메일만 허용됩니다.");
            }
            // 이메일 인증 여부
            status = emailService.isEmailVerified(email) ? AccountStatus.ACTIVE : AccountStatus.PENDING;
        }

        // 3) Account 저장
        Account account = new Account(
                null,
                userId,
                email,
                passwordEncoder.encode(req.password()),
                role,
                status
        );
        accountRepo.save(account);

        // 4) 역할별 프로필 최소 정보 저장 (연관관계로 주입)
        if (role == Role.STUDENT) {
            // UserProfile.create(Account, department, name, phone)
            UserProfile profile = UserProfile.create(account, null, req.name(), null);
            userProfileRepo.save(profile);
        } else {
            // AdminProfile.create(Account, displayName, adminLevel) 라는 팩토리 메서드가 있다고 가정
            AdminProfile profile = AdminProfile.create(account, req.name(), 1);
            adminProfileRepo.save(profile);
        }

        // 5) 응답
        return new SignupResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                account.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        final String idOrEmail = req.usernameOrEmail().trim().toLowerCase();

        // userId 또는 email로 조회 (리포지토리 메서드 명 주의!)
        Account account = accountRepo.findByUserIdOrEmail(idOrEmail, idOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("아이디/이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("아이디/이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("계정이 활성화되지 않았습니다.");
        }

        // 프로필에서 표시명 조회
        String displayName = resolveName(account);

        // 통합 페이로드로 토큰 발급
        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUserId(),
                displayName,
                account.getEmail(),
                account.getRole().name()
        );
        String accessToken = jwtProvider.createToken(principal);

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                accessToken,
                null // refresh 토큰 쓰면 여기에 세팅
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Account account = accountRepo.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("계정이 존재하지 않습니다."));

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUserId(),
                account.getEmail(),
                null,
                null
        );
    }

    private String resolveName(Account account) {
        if (account.getRole() == Role.STUDENT) {
            return userProfileRepo.findByAccountId(account.getId())
                    .map(UserProfile::getName)
                    .orElse(null);
        } else {
            return adminProfileRepo.findByAccountId(account.getId())
                    .map(AdminProfile::getDisplayName)
                    .orElse(null);
        }
    }
}