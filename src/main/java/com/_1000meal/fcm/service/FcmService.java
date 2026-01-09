package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.FcmPlatform;
import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.domain.NotificationPreference;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com._1000meal.fcm.repository.NotificationPreferenceRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository tokenRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * 로그인 사용자만 호출 가능(보안은 SecurityConfig가 보장)
     * - 토큰이 이미 존재하면 accountId를 재연결하고 active=true로 갱신
     * - 토큰이 없으면 신규 생성
     * - preference가 없으면 기본 ON으로 생성 (✅ "로그인하면 기본 ON" 정책)
     */
    @Transactional
    public void registerOrRelinkToken(Long accountId, String token, FcmPlatform platform) {
        String cleanedToken = (token == null) ? "" : token.trim();
        if (cleanedToken.isEmpty()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "FCM token은 필수입니다.");
        }
        FcmPlatform p = (platform == null) ? FcmPlatform.WEB : platform;

        Optional<FcmToken> existing = tokenRepository.findByToken(cleanedToken);
        if (existing.isPresent()) {
            existing.get().relink(accountId, p);
        } else {
            tokenRepository.save(FcmToken.create(accountId, cleanedToken, p));
        }

        // preference 없으면 기본 ON 생성
        preferenceRepository.findByAccountId(accountId)
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.createDefaultOn(accountId)));
    }

    @Transactional(readOnly = true)
    public boolean getEnabled(Long accountId) {
        return preferenceRepository.findByAccountId(accountId)
                .map(NotificationPreference::isEnabled)
                .orElse(true); // 기본 ON
    }

    @Transactional
    public void setEnabled(Long accountId, boolean enabled) {
        NotificationPreference pref = preferenceRepository.findByAccountId(accountId)
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.createDefaultOn(accountId)));

        pref.setEnabled(enabled);
    }
}