package com._1000meal.global.util;

import com._1000meal.global.error.code.AdminSignupErrorCode;
import com._1000meal.global.error.exception.CustomException;

import java.util.regex.Pattern;

public class PasswordValidator {

    // 8~16자, 영문대소문자, 숫자, 특수문자 조합 (사용 가능한 특수문자 32자)
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~])[a-zA-Z\\d!\"#$%&'()*+,\\-./:;<=>?@\begin:math:display$\\\\\\\\\\$end:math:display$^_`{|}~]{8,16}$";


    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static void validatePassword(String password, String username, String phoneNumber) {
        if (password == null || password.contains(" ")) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        // 숫자만/영문만/특수문자만 금지 (혼용 권장)
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!\"#$%&'()*+,-./:;<=>?@\\[₩\\]^_`{|}~].*");
        if (!(hasLetter && hasDigit && hasSpecial)) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        // 동일 문자 3개 이상 연속 금지
        if (password.matches(".*(.)\\1{2,}.*")) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        // username, phoneNumber 포함 금지
        if (username != null && password.toLowerCase().contains(username.toLowerCase())) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        if (phoneNumber != null && password.contains(phoneNumber)) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
        // 연속된 숫자·문자 금지 (ex: 1234, abcd 등)
        if (isSequential(password)) {
            throw new CustomException(AdminSignupErrorCode.PASSWORD_WEAK);
        }
    }

    private static boolean isSequential(String pw) {
        // 연속 3자리 이상 숫자, 문자 (오름/내림)
        for (int i = 0; i < pw.length() - 2; i++) {
            char c1 = pw.charAt(i);
            char c2 = pw.charAt(i + 1);
            char c3 = pw.charAt(i + 2);
            if ((c2 - c1 == 1 && c3 - c2 == 1) || (c1 - c2 == 1 && c2 - c3 == 1)) {
                return true;
            }
        }
        return false;
    }
}