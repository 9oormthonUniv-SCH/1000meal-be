package com._1000meal.userOauth.service;


import com._1000meal.global.error.code.UserLoginErrorCode;
import com._1000meal.global.error.exception.CustomException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    public String getLoginSuccessMessage(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            // 로그인된 유저가 없을 때 예외 던짐
            throw new CustomException(UserLoginErrorCode.USER_NOT_AUTHENTICATED);
        }
        // 필요하면 User 타입으로 캐스팅해서 추가 정보 응답도 가능
        return "로그인에 성공했습니다!";
    }

    public void logout(HttpSession session) {
        session.invalidate(); // 세션 무효화(로그아웃)
    }
}