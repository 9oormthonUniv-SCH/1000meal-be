package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository tokenRepository;

    public void sendStoreOpened(Long storeId, String storeName) {
        // 해당 매장을 즐겨찾기한 사용자 중 알림 ON + active 토큰 보유자만 대상
        List<FcmToken> tokens = tokenRepository.findActiveTokensForFavoriteStore(storeId);
        List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).distinct().toList();

        if (tokenStrings.isEmpty()) {
            log.info("[FCM][OPEN] storeId={}, success=0, failure=0, skipped=no_favorite_subscribers",
                    storeId);
            return;
        }

        String title = "오픈 알림";
        String body  = "[" + storeName + "]이 영업을 시작했어요";

        MulticastMessage msg = MulticastMessage.builder()
                .addAllTokens(tokenStrings)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "OPEN")
                .putData("storeId", String.valueOf(storeId))
                .putData("storeName", storeName)
                .build();

        try {
            BatchResponse res = FirebaseMessaging.getInstance().sendEachForMulticast(msg);
            log.info("[FCM][OPEN] storeId={}, success={}, failure={}",
                    storeId, res.getSuccessCount(), res.getFailureCount());

            // 실패 토큰 정리(선택): invalid/registration-token-not-registered 등일 때 deactivate
            // res.getResponses()를 순회하며 에러코드 보고 처리 가능
        } catch (FirebaseMessagingException e) {
            log.error("[FCM][OPEN] send failed: {}", e.getMessage(), e);
        }
    }
}