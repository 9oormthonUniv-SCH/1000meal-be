package com._1000meal.global.security;

import com._1000meal.adminlogin.entity.AdminEntity;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//
//@Component
//public class JwtProvider {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.access-exp-seconds:3600}")
//    private long accessExpSeconds;
//
//    private SecretKey key() {
//        // 문자열 비밀키 사용 (application.yml 과 동일해야 함)
//        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    /* ===================== 사용자 토큰 ===================== */
//    public String createUserToken(User user) {
//        Date now = new Date();
//        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);
//
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("uid", user.getId());
//        claims.put("userId", user.getUserId());
//        claims.put("name", user.getName());
//        claims.put("email", user.getEmail());
//        claims.put("role", user.getRole().name()); // STUDENT
//
//        return Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setClaims(claims)
//                .setSubject("USER")       // 사용자 토큰 식별
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    /* (선택) 관리자 토큰이 필요하면 따로 분리해두세요 */
//    public String createAdminToken(AdminEntity admin) {
//        Date now = new Date();
//        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);
//
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("aid", admin.getId());
//        claims.put("username", admin.getUsername());
//        claims.put("name", admin.getName());
//        claims.put("role", "ADMIN");
//
//        return Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setClaims(claims)
//                .setSubject("ADMIN")
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // ★ 추가: 토큰 유효성 검사
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(key())
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    // Claims 꺼내기
//    public Claims parse(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-exp-seconds:3600}")
    private long accessExpSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 통일된 JWT 발급 */
    public String createToken(AuthPrincipal p) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);

        // ---- 통합 클레임 ----
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", p.id());
        claims.put("account", p.account());
        claims.put("name", p.name());
        claims.put("email", p.email());     // admin은 null 가능
        claims.put("role", p.role());       // "STUDENT" | "ADMIN"

        // ---- [선택] 과도기 호환 키 (안정화 후 제거) ----
        if ("STUDENT".equals(p.role())) {
            claims.put("uid", p.id());
            claims.put("userId", p.account());
        } else if ("ADMIN".equals(p.role())) {
            claims.put("aid", p.id());
            claims.put("username", p.account());
        }

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setSubject("AUTH")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 유효성 검사 */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) { return false; }
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }
}
