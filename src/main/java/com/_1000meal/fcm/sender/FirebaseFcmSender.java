package com._1000meal.fcm.sender;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FirebaseFcmSender implements FcmSender {

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
            List<FcmSendFailure> failures = collectFailures(response, tokens);
            return new FcmSendResult(response.getSuccessCount(), response.getFailureCount(), failures);
        } catch (Exception e) {
            throw new IllegalStateException("Firebase send failed", e);
        }
    }

    private List<FcmSendFailure> collectFailures(BatchResponse response, List<String> tokenStrings) {
        List<FcmSendFailure> failures = new ArrayList<>();
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }
            FirebaseMessagingException ex = sendResponse.getException();
            String messagingErrorCode = ex != null && ex.getMessagingErrorCode() != null
                    ? ex.getMessagingErrorCode().name()
                    : null;
            String errorCode = ex != null && ex.getErrorCode() != null
                    ? ex.getErrorCode().name()
                    : null;
            String message = ex != null ? ex.getMessage() : "unknown FCM send failure";
            failures.add(new FcmSendFailure(tokenStrings.get(i), messagingErrorCode, errorCode, message));
        }
        return failures;
    }
}
