package com._1000meal.auth.model;

import com._1000meal.global.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter @NoArgsConstructor
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학번/아이디 — DB의 username 컬럼에 매핑
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT / ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status; // ACTIVE, PENDING, SUSPENDED ...

    public Account(Long id, String userId, String email, String passwordHash, Role role, AccountStatus status) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }


    public void activate() { this.status = AccountStatus.ACTIVE; }
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
    public void deleteAndReleaseIdentifiers() {
        // 상태만 바꾸면 유니크를 계속 점유하니 email/userId를 tombstone으로 바꿔줍니다.
        String suffix = ".deleted." + this.id + "." + System.currentTimeMillis();

        // email 보존 정책: 도메인 유지(로그 남길 필요 있으면 별도 audit에 저장)
        if (this.email != null) {
            int at = this.email.indexOf('@');
            if (at > 0) {
                String local = this.email.substring(0, at);
                String domain = this.email.substring(at);
                this.email = local + suffix + domain; // ex) user+deleted.123.169....@sch.ac.kr
            } else {
                this.email = this.email + suffix;
            }
        }
        if (this.userId != null) {
            this.userId = this.userId + suffix;     // 학번(아이디)도 동일 처리
        }
        this.status = AccountStatus.DELETED;
    }

}