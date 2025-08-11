package com._1000meal.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학번/로그인 아이디
    @Column(nullable = false, unique = true, length = 30)
    private String userId;

    @Column(nullable = false, length = 60) // BCrypt
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // STUDENT, ADMIN 등

    @Column(nullable = false)
    private boolean isNotificationEnabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private User(String userId, String password, String name, String email, UserRole role) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isNotificationEnabled = false;
        this.createdAt = LocalDateTime.now();
    }

    public static User create(String userId, String encodedPw, String name, String email) {
        return new User(userId, encodedPw, name, email, UserRole.STUDENT);
    }
}