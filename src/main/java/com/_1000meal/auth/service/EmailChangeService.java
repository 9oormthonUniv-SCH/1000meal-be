package com._1000meal.auth.service;

import com._1000meal.auth.dto.EmailChange.*;

public interface EmailChangeService {
    EmailChangeStartResponse start(Long accountId, EmailChangeStartRequest req);
    void requestCode(Long accountId, EmailChangeRequestCodeRequest req);
    EmailChangeVerifyResponse verify(Long accountId, EmailChangeVerifyRequest req);
}