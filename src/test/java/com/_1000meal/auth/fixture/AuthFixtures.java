package com._1000meal.auth.fixture;

import com._1000meal.auth.dto.SignupRequest;
import com._1000meal.global.constant.Role;

public final class AuthFixtures {
    public static final String VALID_PW = "Str0ng!Pw9A";

    public static SignupRequest studentSignup() {
        return new SignupRequest(Role.STUDENT, "20250001", "주흔", "test@sch.ac.kr", VALID_PW, null);
    }

    private AuthFixtures() {}
}