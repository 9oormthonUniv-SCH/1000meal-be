package com._1000meal.global.security;

import com._1000meal.auth.model.AuthPrincipal;
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

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-exp-seconds:3600}")
    private long accessExpSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /* ===================== 토큰 생성 ===================== */

    /** 기본: 추가 클레임 없이 발급 */
    public String createToken(AuthPrincipal p) {
        return createToken(p, null);
    }

    /** 확장: 추가 클레임(storeId, storeName 등) 포함 발급 */
    public String createToken(AuthPrincipal p, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);

        // 공통(통합) 클레임
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",       p.id());        // account PK
        claims.put("username", p.account());  // userId(학번) 또는 관리자ID
        claims.put("name",     p.name());
        claims.put("email",    p.email());     // 관리자면 null 가능
        claims.put("role",     p.role());      // "STUDENT" | "ADMIN"

        // 과도기 호환 키(안정화 후 제거 가능)
        if ("STUDENT".equals(p.role())) {
            claims.put("uid",    p.id());
            claims.put("userId", p.account());   // 학번/아이디
        } else if ("ADMIN".equals(p.role())) {
            claims.put("aid",      p.id());
            claims.put("username", p.account()); // 관리자 로그인 ID
        }

        // 추가 클레임 병합 (예: storeId, storeName)
        if (extraClaims != null && !extraClaims.isEmpty()) {
            claims.putAll(extraClaims);
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

    /* ===================== 검증/파싱 ===================== */

    /** 현재 코드와의 호환을 위해 validate/validateToken 둘 다 제공 */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        return validate(token);
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }
}