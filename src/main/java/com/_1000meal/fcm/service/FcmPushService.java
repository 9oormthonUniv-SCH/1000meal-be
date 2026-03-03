package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.FcmToken;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.message.FcmMessageFactory;
import com._1000meal.fcm.sender.FcmSendFailure;
import com._1000meal.fcm.repository.FcmTokenRepository;
import com._1000meal.fcm.sender.FcmSendResult;
import com._1000meal.fcm.sender.FcmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository tokenRepository;
    private final FcmSender fcmSender;

    public void sendOpenNotification(Long accountId, Long storeId, String storeName, String storeImageUrl) {
        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.OPEN, storeName, null, 0);

        Map<String, String> data = new HashMap<>();
        data.put("type", "OPEN");
        data.put("storeId", String.valueOf(storeId));
        data.put("storeName", storeName);
        data.put("imageUrl", storeImageUrl == null ? "" : storeImageUrl);

        sendMulticastForAccount(accountId, "[FCM][OPEN]", message.title(), message.body(), data);
    }

    public void sendStoreOpened(Long storeId, String storeName) {
        List<FcmToken> tokens = tokenRepository.findActiveTokensForFavoriteStore(storeId);
        List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).distinct().toList();

        if (tokenStrings.isEmpty()) {
            log.info("[FCM][OPEN] storeId={}, success=0, failure=0, skipped=no_favorite_subscribers",
                    storeId);
            return;
        }

        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.OPEN, storeName, null, 0);

        try {
            FcmSendResult result = fcmSender.sendMulticast(
                    tokenStrings,
                    message.title(),
                    message.body(),
                    Map.of(
                            "type", "OPEN",
                            "storeId", String.valueOf(storeId),
                            "storeName", storeName
                    )
            );
            log.info("[FCM][OPEN] storeId={}, success={}, failure={}",
                    storeId, result.successCount(), result.failureCount());
            handleSendFailures("[FCM][OPEN]", result.failures(), accountIdByToken(tokens));
        } catch (Exception e) {
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

        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.LOW_STOCK_30, storeName, groupName, remaining);

        try {
            FcmSendResult result = fcmSender.sendMulticast(
                    tokenStrings,
                    message.title(),
                    message.body(),
                    Map.of(
                            "type", "LOW_STOCK_30",
                            "storeId", String.valueOf(storeId),
                            "storeName", storeName,
                            "groupId", String.valueOf(groupId),
                            "groupName", groupName,
                            "stock", String.valueOf(remaining)
                    )
            );
            log.info("[FCM][LOW_STOCK_30] storeId={}, groupId={}, success={}, failure={}",
                    storeId, groupId, result.successCount(), result.failureCount());
            handleSendFailures("[FCM][LOW_STOCK_30]", result.failures(), accountIdByToken(tokens));
        } catch (Exception e) {
            log.error("[FCM][LOW_STOCK_30] send failed: {}", e.getMessage(), e);
        }
    }

    public void sendLowStock30NotificationForAccount(
            Long accountId,
            Long storeId,
            String storeName,
            Long groupId,
            String groupName,
            int remaining
    ) {
        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.LOW_STOCK_30, storeName, groupName, remaining);

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOW_STOCK_30");
        data.put("storeId", String.valueOf(storeId));
        data.put("storeName", storeName);
        data.put("groupId", String.valueOf(groupId));
        data.put("groupName", groupName);
        data.put("stock", String.valueOf(remaining));

        sendMulticastForAccount(accountId, "[FCM][LOW_STOCK_30]", message.title(), message.body(), data);
    }

    public void sendStockDeadlineNotification(
            Long accountId,
            Long storeId,
            String storeName,
            String storeImageUrl,
            Long menuGroupId,
            String menuGroupName,
            int remaining
    ) {
        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.STOCK_DEADLINE, storeName, menuGroupName, remaining);

        Map<String, String> data = new HashMap<>();
        data.put("type", "STOCK_DEADLINE");
        data.put("storeId", String.valueOf(storeId));
        data.put("storeName", storeName == null ? "" : storeName);
        data.put("menuGroupId", String.valueOf(menuGroupId));
        data.put("menuGroupName", menuGroupName == null ? "" : menuGroupName);
        data.put("remain", String.valueOf(remaining));
        data.put("imageUrl", storeImageUrl == null ? "" : storeImageUrl);

        sendMulticastForAccount(accountId, "[FCM][STOCK_DEADLINE]", message.title(), message.body(), data);
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

        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.LOW_STOCK_10, storeName, groupName, remaining);

        try {
            FcmSendResult result = fcmSender.sendMulticast(
                    tokenStrings,
                    message.title(),
                    message.body(),
                    Map.of(
                            "type", "LOW_STOCK",
                            "storeId", String.valueOf(storeId),
                            "storeName", storeName,
                            "groupId", String.valueOf(groupId),
                            "groupName", groupName,
                            "remaining", String.valueOf(remaining)
                    )
            );
            log.info("[FCM][LOW_STOCK] storeId={}, groupId={}, success={}, failure={}",
                    storeId, groupId, result.successCount(), result.failureCount());
            handleSendFailures("[FCM][LOW_STOCK]", result.failures(), accountIdByToken(tokens));
        } catch (Exception e) {
            log.error("[FCM][LOW_STOCK] send failed: {}", e.getMessage(), e);
        }
    }

    public void sendLowStockNotificationForAccount(
            Long accountId,
            Long storeId,
            String storeName,
            Long groupId,
            String groupName,
            int remaining
    ) {
        FcmMessageFactory.FcmMessage message =
                FcmMessageFactory.of(NotificationType.LOW_STOCK_10, storeName, groupName, remaining);

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOW_STOCK");
        data.put("storeId", String.valueOf(storeId));
        data.put("storeName", storeName);
        data.put("groupId", String.valueOf(groupId));
        data.put("groupName", groupName);
        data.put("remaining", String.valueOf(remaining));

        sendMulticastForAccount(accountId, "[FCM][LOW_STOCK]", message.title(), message.body(), data);
    }

    public void sendWeeklyMenuUploadedNotification(
            Long accountId,
            Long storeId,
            String storeName,
            Long groupId,
            String groupName,
            String imageUrl,
            String weekKey
    ) {
        String title = "이번 주 메뉴가 올라왔어요";
        String body = "[" + groupName + "]에 천원의 아침밥 메뉴를 확인해보세요\n" +
                "→ 월~금까지 메뉴가 채워졌을 시 알림";

        Map<String, String> data = new HashMap<>();
        data.put("type", "WEEKLY_MENU_UPLOADED");
        data.put("storeId", String.valueOf(storeId));
        data.put("storeName", storeName);
        data.put("groupId", String.valueOf(groupId));
        data.put("groupName", groupName);
        data.put("imageUrl", imageUrl == null ? "" : imageUrl);
        data.put("weekKey", weekKey == null ? "" : weekKey);

        sendMulticastForAccount(accountId, "[FCM][WEEKLY_MENU_UPLOADED]", title, body, data);
    }

    private void sendMulticastForAccount(
            Long accountId,
            String logPrefix,
            String title,
            String body,
            Map<String, String> data
    ) {
        List<FcmToken> tokens = tokenRepository.findAllByAccountIdAndActiveTrue(accountId);
        List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).distinct().toList();

        if (tokenStrings.isEmpty()) {
            log.info("{} accountId={}, skipped=no_active_tokens", logPrefix, accountId);
            return;
        }

        try {
            FcmSendResult result = fcmSender.sendMulticast(tokenStrings, title, body, data);
            log.info("{} accountId={}, success={}, failure={}",
                    logPrefix, accountId, result.successCount(), result.failureCount());
            handleSendFailures(logPrefix, result.failures(), accountIdByToken(tokens));
        } catch (Exception e) {
            log.error("{} send failed: {}", logPrefix, e.getMessage(), e);
        }
    }

    private Map<String, Long> accountIdByToken(List<FcmToken> tokens) {
        return tokens.stream()
                .collect(Collectors.toMap(
                        FcmToken::getToken,
                        FcmToken::getAccountId,
                        (left, right) -> left
                ));
    }

    private void handleSendFailures(
            String logPrefix,
            List<FcmSendFailure> failures,
            Map<String, Long> accountIdByToken
    ) {
        if (failures == null || failures.isEmpty()) {
            return;
        }

        for (FcmSendFailure failure : failures) {
            String messagingErrorCode = failure.messagingErrorCode() == null
                    ? "UNKNOWN"
                    : failure.messagingErrorCode();
            Long accountId = accountIdByToken.get(failure.token());
            String tokenPrefix = maskToken(failure.token());

            if ("UNREGISTERED".equals(messagingErrorCode) || "INVALID_ARGUMENT".equals(messagingErrorCode)) {
                int updated = tokenRepository.deactivateByToken(failure.token());
                log.warn("{}[FAIL] accountId={} tokenPrefix={} messagingErrorCode={} errorCode={} msg={} action=deactivate updated={}",
                        logPrefix,
                        accountId,
                        tokenPrefix,
                        messagingErrorCode,
                        failure.errorCode(),
                        failure.message(),
                        updated);
                continue;
            }

            if ("THIRD_PARTY_AUTH_ERROR".equals(messagingErrorCode)) {
                log.warn("{}[FAIL] accountId={} tokenPrefix={} messagingErrorCode={} errorCode={} msg={} action=keep_token note=possible_apns_config_issue",
                        logPrefix,
                        accountId,
                        tokenPrefix,
                        messagingErrorCode,
                        failure.errorCode(),
                        failure.message());
                continue;
            }

            log.warn("{}[FAIL] accountId={} tokenPrefix={} messagingErrorCode={} errorCode={} msg={}",
                    logPrefix,
                    accountId,
                    tokenPrefix,
                    messagingErrorCode,
                    failure.errorCode(),
                    failure.message());
        }
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "(empty)";
        }
        int prefixLength = Math.min(16, token.length());
        return token.substring(0, prefixLength) + "...";
    }
}
