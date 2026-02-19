package com._1000meal.fcm.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConditionalOnMissingBean(FcmSender.class)
@Component
public class NoopFcmSender implements FcmSender {
    private static final Logger log = LoggerFactory.getLogger(NoopFcmSender.class);

    @Override
    public FcmSendResult sendMulticast(List<String> tokens,
                                       String title,
                                       String body,
                                       Map<String, String> data) {
        log.info("[FCM-NOOP] skip sending push tokens={}, title='{}', body='{}', data={}",
                tokens != null ? tokens.size() : 0, title, body, data);
        return new FcmSendResult(0, 0);
    }
}
