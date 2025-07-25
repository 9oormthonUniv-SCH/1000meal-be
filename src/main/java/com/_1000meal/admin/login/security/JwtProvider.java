package com._1000meal.admin.login.security;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private final String SECRET_KEY = "your-secret-key"; // 보안을 위해 환경변수로 빼도 OK
    private final long EXPIRATION = 1000L * 60 * 60; // 1시간

    public String createToken(Long adminId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("adminId", adminId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}