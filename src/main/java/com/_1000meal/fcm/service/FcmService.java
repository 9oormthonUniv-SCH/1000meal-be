package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.FcmPlatform;
import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.domain.NotificationPreference;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com._1000meal.fcm.repository.NotificationPreferenceRepository;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1) 토큰 upsert (동시성/유니크 충돌 방어)
        try {
            tokenRepository.findByToken(cleanedToken)
                    .ifPresentOrElse(
                            t -> t.relink(accountId, p),
                            () -> tokenRepository.save(FcmToken.create(accountId, cleanedToken, p))
                    );
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 unique(token) 충돌이 날 수 있음 → 다시 조회해서 relink로 수습
            FcmToken existing = tokenRepository.findByToken(cleanedToken)
                    .orElseThrow(() -> e);
            existing.relink(accountId, p);
        }

        // 2) preference 기본 ON 보장 (동시성/유니크 충돌 방어)
        ensurePreferenceExistsDefaultOn(accountId);
    }

    @Transactional(readOnly = true)
    public boolean getEnabled(Long accountId) {
        return preferenceRepository.findByAccountId(accountId)
                .map(NotificationPreference::isEnabled)
                .orElse(true); // 정책: 기본 ON
    }

    @Transactional
    public void setEnabled(Long accountId, boolean enabled) {
        NotificationPreference pref = ensurePreferenceExistsDefaultOn(accountId);
        pref.setEnabled(enabled);
    }

    private NotificationPreference ensurePreferenceExistsDefaultOn(Long accountId) {
        return preferenceRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    try {
                        return preferenceRepository.save(NotificationPreference.createDefaultOn(accountId));
                    } catch (DataIntegrityViolationException e) {
                        // 동시성으로 unique(account_id) 충돌 → 다시 조회해서 반환
                        return preferenceRepository.findByAccountId(accountId)
                                .orElseThrow(() -> e);
                    }
                });
    }

    @Transactional
    public void ensureDefaultPreferenceOn(Long accountId) {
        preferenceRepository.findByAccountId(accountId)
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.createDefaultOn(accountId)));
    }

}