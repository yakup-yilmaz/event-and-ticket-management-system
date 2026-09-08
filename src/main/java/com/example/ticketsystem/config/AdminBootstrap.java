package com.example.ticketsystem.config;

import com.example.ticketsystem.entity.Role;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Uygulama ayağa kalkınca varsayılan ADMIN yoksa oluşturur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@ticketsystem.local}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = adminEmail != null ? adminEmail.trim().toLowerCase(java.util.Locale.ROOT) : "";
        if (!userRepository.existsByEmail(normalizedEmail)) {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException("Uygulama güvenliği için ADMIN_PASSWORD tanımlanmalıdır! Boş veya varsayılan parola kabul edilmez.");
            }
            userRepository.save(User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build());
            log.info("Varsayılan ADMIN oluşturuldu: {}", normalizedEmail);
        }
    }
}
