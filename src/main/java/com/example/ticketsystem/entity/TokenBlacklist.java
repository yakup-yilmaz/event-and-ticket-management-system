package com.example.ticketsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Logout sonrası access JWT'nin jti değerini kara listeye alır.
 * Access token süresi dolunca kayıt temizlenebilir (opsiyonel scheduled job).
 */
@Entity
@Table(
        name = "token_blacklist",
        indexes = {@Index(name = "idx_blacklist_jti", columnList = "jti", unique = true)}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private LocalDateTime blacklistedAt;

    @PrePersist
    void onCreate() {
        blacklistedAt = LocalDateTime.now();
    }
}
