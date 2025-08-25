package com._1000meal.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name="admin_profile")
@Getter
@NoArgsConstructor
public class AdminProfile {
    @Id
    private Long accountId;

    private String displayName;
    private Integer adminLevel;

    public AdminProfile(Long accountId, String displayName, Integer adminLevel) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.adminLevel = adminLevel;
    }
}