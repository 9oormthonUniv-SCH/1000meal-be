package com._1000meal.auth.refresh;

import com._1000meal.auth.dto.RefreshTokenResponse;
import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.model.AdminProfile;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.model.UserProfile;
import com._1000meal.auth.repository.AdminProfileRepository;
import com._1000meal.auth.repository.UserProfileRepository;
import com._1000meal.global.constant.Role;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final UserProfileRepository userProfileRepository;
    private final AdminProfileRepository adminProfileRepository;

    @Transactional
    public String issueOnLogin(Account account, String deviceId, String userAgent, String ipAddress) {
        String refreshToken = jwtProvider.createRefreshToken(account);
        String tokenHash = jwtProvider.sha256Hex(refreshToken);
        LocalDateTime expiresAt = now().plusDays(jwtProvider.getRefreshExpDays());

        RefreshToken entity = RefreshToken.issue(
                account,
                tokenHash,
                expiresAt,
                deviceId,
                userAgent,
                ipAddress
        );
        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    @Transactional
    public RefreshTokenResponse refresh(String rawRefreshToken) {
        Claims claims = jwtProvider.parseAndValidateRefreshToken(rawRefreshToken);
        String tokenHash = jwtProvider.sha256Hex(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        LocalDateTime now = now();
        if (refreshToken.isRevoked()) {
            throw new CustomException(ErrorCode.REVOKED_REFRESH_TOKEN);
        }
        if (refreshToken.isExpired(now)) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Long subjectAccountId = parseAccountId(claims.getSubject());
        if (!refreshToken.getAccount().getId().equals(subjectAccountId)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (refreshToken.getAccount().getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
        }

        refreshToken.markUsed(now);
        String accessToken = createAccessToken(refreshToken.getAccount());
        return new RefreshTokenResponse(accessToken, jwtProvider.getAccessExpSeconds());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = jwtProvider.sha256Hex(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> token.revoke(now()));
    }

    private String createAccessToken(Account account) {
        String displayName = resolveName(account);
        Long storeId = null;
        String storeName = null;
        if (account.getRole() == Role.ADMIN) {
            AdminProfile profile = adminProfileRepository.findByAccountId(account.getId()).orElse(null);
            if (profile != null && profile.getStore() != null) {
                storeId = profile.getStore().getId();
                storeName = profile.getStore().getName();
            }
        }

        AuthPrincipal principal = new AuthPrincipal(
                account.getId(),
                account.getUserId(),
                displayName,
                account.getEmail(),
                account.getRole().name()
        );

        Map<String, Object> extra = (storeId == null) ? null : Map.of("storeId", storeId, "storeName", storeName);
        return jwtProvider.createToken(principal, extra);
    }

    private String resolveName(Account account) {
        if (account.getRole() == Role.STUDENT) {
            return userProfileRepository.findByAccountId(account.getId())
                    .map(UserProfile::getName)
                    .orElse(null);
        }
        return adminProfileRepository.findByAccountId(account.getId())
                .map(AdminProfile::getDisplayName)
                .orElse(null);
    }

    private Long parseAccountId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(KST);
    }
}
