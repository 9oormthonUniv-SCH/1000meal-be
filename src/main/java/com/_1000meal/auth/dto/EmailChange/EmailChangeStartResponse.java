package com._1000meal.auth.dto.EmailChange;



public record EmailChangeStartResponse(
        String changeId,
        long expiresInSec
) {}