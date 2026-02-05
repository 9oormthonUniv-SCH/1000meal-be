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

    /**
     * 재고 30 이하 알림 발송
     * 해당 매장을 즐겨찾기한 사용자 중 알림 ON + active 토큰 보유자에게 푸시
     */
    public void sendLowStock30Notification(Long storeId, String storeName,
                                            Long groupId, String groupName, int remaining) {
        List<FcmToken> tokens = tokenRepository.findActiveTokensForFavoriteStore(storeId);
        List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).distinct().toList();

        if (tokenStrings.isEmpty()) {
            log.info("[FCM][LOW_STOCK_30] storeId={}, groupId={}, success=0, failure=0, skipped=no_favorite_subscribers",
                    storeId, groupId);
            return;
        }

        String title = "재고 알림";
        String body = String.format("[%s] %s 수량이 30개 이하로 줄었어요!", storeName, groupName);

        MulticastMessage msg = MulticastMessage.builder()
                .addAllTokens(tokenStrings)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "LOW_STOCK_30")
                .putData("storeId", String.valueOf(storeId))
                .putData("storeName", storeName)
                .putData("groupId", String.valueOf(groupId))
                .putData("groupName", groupName)
                .putData("stock", String.valueOf(remaining))
                .build();

        try {
            BatchResponse res = FirebaseMessaging.getInstance().sendEachForMulticast(msg);
            log.info("[FCM][LOW_STOCK_30] storeId={}, groupId={}, success={}, failure={}",
                    storeId, groupId, res.getSuccessCount(), res.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("[FCM][LOW_STOCK_30] send failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 품절 임박 알림 발송
     * 해당 매장을 즐겨찾기한 사용자 중 알림 ON + active 토큰 보유자에게 푸시
     */
    public void sendLowStockNotification(Long storeId, String storeName,
                                          Long groupId, String groupName, int remaining) {
        List<FcmToken> tokens = tokenRepository.findActiveTokensForFavoriteStore(storeId);
        List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).distinct().toList();

        if (tokenStrings.isEmpty()) {
            log.info("[FCM][LOW_STOCK] storeId={}, groupId={}, success=0, failure=0, skipped=no_favorite_subscribers",
                    storeId, groupId);
            return;
        }

        String title = "품절 임박 알림";
        String body = String.format("[%s] 수량이 곧 품절돼요! (남은 수량: %d개)", storeName, remaining);

        MulticastMessage msg = MulticastMessage.builder()
                .addAllTokens(tokenStrings)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "LOW_STOCK")
                .putData("storeId", String.valueOf(storeId))
                .putData("storeName", storeName)
                .putData("groupId", String.valueOf(groupId))
                .putData("groupName", groupName)
                .putData("remaining", String.valueOf(remaining))
                .build();

        try {
            BatchResponse res = FirebaseMessaging.getInstance().sendEachForMulticast(msg);
            log.info("[FCM][LOW_STOCK] storeId={}, groupId={}, success={}, failure={}",
                    storeId, groupId, res.getSuccessCount(), res.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("[FCM][LOW_STOCK] send failed: {}", e.getMessage(), e);
        }
    }
}