package com._1000meal.fcm.sender;

import java.util.List;
import java.util.Map;

public interface FcmSender {
    FcmSendResult sendMulticast(List<String> tokens,
                                String title,
                                String body,
                                Map<String, String> data);
}
