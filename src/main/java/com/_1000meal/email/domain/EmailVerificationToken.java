package com._1000meal.email.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String email;

    @Column(nullable=false, length = 6)
    private String code;

    @Column(nullable=false)
    private LocalDateTime expiredAt;

    @Column(nullable=false)
    private boolean verified;

    @Column(nullable=false, updatable = false)
    private LocalDateTime createdAt;   // ★ 추가

    private EmailVerificationToken(String email, String code, LocalDateTime expiredAt) {
        this.email = email;
        this.code = code;
        this.expiredAt = expiredAt;
        this.verified = false;
        this.createdAt = LocalDateTime.now(); // ★ 생성 시각 기록
    }

    public static EmailVerificationToken create(String email, String code, int minutes) {
        return new EmailVerificationToken(email, code, LocalDateTime.now().plusMinutes(minutes));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public void markVerified() {
        this.verified = true;
    }

    // ★ 추가: 명시적 만료
    public void expire() {
        this.expiredAt = LocalDateTime.now().minusSeconds(1);
    }
}