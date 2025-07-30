package com._1000meal.user.service;


import com._1000meal.user.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public String getLoginSuccessMessage(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user != null) {
            return "로그인 성공! " + user.toString();
        } else {
            return "로그인 성공했지만 사용자 정보가 없습니다.";
        }
    }
}