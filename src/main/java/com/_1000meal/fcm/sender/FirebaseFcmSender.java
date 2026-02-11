package com._1000meal.fcm.sender;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Profile({"prod", "dev-firebase"})
@Component
@RequiredArgsConstructor
public class FirebaseFcmSender implements FcmSender {

    private final InvalidTokenHandler invalidTokenHandler;

    @Override
    public FcmSendResult sendMulticast(List<String> tokens,
                                       String title,
                                       String body,
                                       Map<String, String> data) {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            handleInvalidTokens(response, tokens);
            return new FcmSendResult(response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            throw new IllegalStateException("Firebase send failed", e);
        }
    }

    private void handleInvalidTokens(BatchResponse response, List<String> tokenStrings) {
        List<String> invalidTokens = new ArrayList<>();
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }
            if (sendResponse.getException() != null
                    && sendResponse.getException().getMessagingErrorCode()
                    == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED) {
                invalidTokens.add(tokenStrings.get(i));
            }
        }
        if (!invalidTokens.isEmpty()) {
            invalidTokenHandler.handleInvalidTokens(invalidTokens);
        }
    }
}
