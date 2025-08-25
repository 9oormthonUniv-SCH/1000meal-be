package com._1000meal.auth.service;

import com._1000meal.auth.dto.LoginRequest;
import com._1000meal.auth.dto.LoginResponse;
import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.auth.dto.SignupResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AdminProfile;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.model.UserProfile;
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
        if (req.role() == null) {
            throw new IllegalArgumentException("역할(role)은 필수입니다.");
        }
        final String username = req.userId().trim();
        final String email = req.email().trim().toLowerCase();
        final Role role = req.role();

        // 1) 유니크 검사(분리)
        if (accountRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (accountRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2) 역할별 정책
        String status = "ACTIVE";
        if (role == Role.STUDENT) {
            // 학번 형식
            if (!username.matches("^\\d{8}$")) {
                throw new IllegalArgumentException("학번 형식이 올바르지 않습니다.");
            }
            // 도메인
            if (!email.endsWith("@sch.ac.kr")) {
                throw new IllegalArgumentException("학생 계정은 @sch.ac.kr 이메일만 허용됩니다.");
            }
            // 이메일 인증 여부에 따라 상태 결정
            status = emailService.isEmailVerified(email) ? "ACTIVE" : "PENDING";
        }

        // 3) Account 저장
        var account = new Account(
                null,
                username,
                email,
                passwordEncoder.encode(req.password()),
                role,
                status
        );
        accountRepo.save(account);

        // 4) 역할별 프로필 – 최소정보(name만)
        if (role == Role.STUDENT) {
            // UserProfile(accountId, department, name, phone) 시그니처라면 나머지는 null
            userProfileRepo.save(new UserProfile(account.getId(), null, req.name(), null));
        } else {
            // AdminProfile(accountId, displayName, adminLevel) 시그니처라면 기본레벨 1
            adminProfileRepo.save(new AdminProfile(account.getId(), req.name(), 1));
        }

        // 5) 응답
        return new SignupResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                account.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        final String idOrEmail = req.usernameOrEmail().trim().toLowerCase();

        // role 없이 username/email 중 하나로 조회
        var account = accountRepo.findByUsernameOrEmail(idOrEmail, idOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("아이디/이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("아이디/이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new IllegalStateException("계정이 활성화되지 않았습니다.");
        }

        // 이름 조회(프로필에서)
        String displayName = resolveName(account);

        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUsername(),
                displayName,
                account.getEmail(),
                account.getRole().name()
        );
        String accessToken = jwtProvider.createToken(principal);

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                accessToken,
                null
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        var account = accountRepo.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("계정이 존재하지 않습니다."));

        return new LoginResponse(
                account.getId(),
                account.getRole(),
                account.getUsername(),
                account.getEmail(),
                null,
                null
        );
    }

    // 프로필에서 이름 조회 – 리포지토리 메서드가 accountId 기준으로 있는 것을 권장
    private String resolveName(Account account) {
        if (account.getRole() == Role.STUDENT) {
            // findByAccountId(...) 메서드가 없다면 findById(accountId) 사용(프로필 PK=account_id인 경우)
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