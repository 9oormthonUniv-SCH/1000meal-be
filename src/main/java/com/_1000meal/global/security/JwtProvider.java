package com._1000meal.global.security;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-exp-minutes:30}")
    private long accessExpMinutes;

    @Value("${jwt.refresh-exp-days:30}")
    private long refreshExpDays;

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
        Date exp = new Date(now.getTime() + getAccessExpSeconds() * 1000);

        // 공통(통합) 클레임
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",       p.id());        // account PK
        claims.put("account",  p.account());   // ★ 표준 키: 학번/관리자ID
        claims.put("name",     p.name());
        claims.put("email",    p.email());     // 관리자면 null 가능
        claims.put("role",     p.role());      // "STUDENT" | "ADMIN"

// ─── 과도기 호환 키(안정화 후 제거 가능) ───
        claims.put("username", p.account());   // 기존 코드 호환
        if ("STUDENT".equals(p.role())) {
            claims.put("uid",    p.id());
            claims.put("userId", p.account()); // 학번
        } else if ("ADMIN".equals(p.role())) {
            claims.put("aid",    p.id());
            // username은 위에서 이미 넣음
        }

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

    public String createRefreshToken(Account account) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + (refreshExpDays * 24 * 60 * 60 * 1000));

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("role", account.getRole().name());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setSubject(account.getId().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ===================== 검증/파싱 ===================== */

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 과거 호출부 호환용
    public boolean validateToken(String token) {
        return validate(token);
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }

    public Claims parseAndValidateRefreshToken(String token) {
        try {
            Claims claims = parse(token);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (CustomException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 hashing failed", e);
        }
    }

    public long getAccessExpSeconds() {
        return accessExpMinutes * 60;
    }

    public long getRefreshExpDays() {
        return refreshExpDays;
    }

    public LocalDateTime refreshExpiresAtFromNow() {
        return LocalDateTime.now(KST).plusDays(refreshExpDays);
    }
}
