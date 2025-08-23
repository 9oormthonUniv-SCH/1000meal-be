package com._1000meal.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name="user_profile")
@Getter
@NoArgsConstructor
public class UserProfile {
    @Id
    private Long accountId; // 1:1 공유 PK 전략도 가능

    private String department;
    private String name;
    private String phone;

    public UserProfile(Long accountId, String department, String name, String phone) {
        this.accountId = accountId;
        this.department = department;
        this.name = name;
        this.phone = phone;
    }
}