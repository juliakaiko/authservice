package com.mymicroservice.authservice.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    @Column(name = "token_id", nullable = false, updatable = false)
    private Long tokenId;

    @Column(name = "user_email", nullable = false, length = 200)
    private String userEmail;

    @Column(name = "refresh_token", nullable = false, length = 1000)
    private String refreshToken;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
