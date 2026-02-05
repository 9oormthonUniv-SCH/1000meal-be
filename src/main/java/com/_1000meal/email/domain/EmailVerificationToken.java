package com._1000meal.email.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens",
        indexes = {
                @Index(name = "idx_email_verified", columnList = "email, verified"),
                @Index(name = "idx_email_created", columnList = "email, createdAt")
        }
)
@Getter
@NoArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 인증 대상 이메일 */
    @Column(nullable = false, length = 255)
    private String email;

    /** 6자리 인증 코드 */
    @Column(nullable = false, length = 6)
    private String code;

    /** 인증 성공 여부 */
    @Column(nullable = false)
    private boolean verified = false;

    /** 만료 시각 */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** 생성 시각 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** ====== 정적 팩토리 메서드 ====== */
    public static EmailVerificationToken create(String email, String code, int minutesToExpire) {
        EmailVerificationToken t = new EmailVerificationToken();
        t.email = email;
        t.code = code;
        t.expiresAt = LocalDateTime.now().plusMinutes(minutesToExpire);
        t.verified = false;
        return t;
    }

    /** 만료 여부 확인 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** 인증 성공 처리 */
    public void markVerified() {
        this.verified = true;
    }
}