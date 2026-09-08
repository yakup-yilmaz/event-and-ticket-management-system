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

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = adminEmail != null ? adminEmail.trim().toLowerCase(java.util.Locale.ROOT) : "";
        if (!userRepository.existsByEmail(normalizedEmail)) {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException("Uygulama güvenliği için ADMIN_PASSWORD tanımlanmalıdır! Boş parola kabul edilmez.");
            }
            if (adminPassword.length() < 8) {
                throw new IllegalStateException("ADMIN_PASSWORD en az 8 karakter uzunluğunda olmalıdır!");
            }
            if (("prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile))
                    && ("ChangeMe123!".equalsIgnoreCase(adminPassword)
                        || "admin".equalsIgnoreCase(adminPassword)
                        || "password".equalsIgnoreCase(adminPassword))) {
                throw new IllegalStateException("Production profilinde bilinen varsayılan ADMIN_PASSWORD kullanılamaz! Lütfen güçlü bir parola belirleyin.");
            }
            if ("ChangeMe123!".equals(adminPassword)) {
                log.warn("GÜVENLİK UYARISI: Varsayılan geliştirme parolası (ChangeMe123!) kullanılmaktadır. Canlı ortamda mutlaka değiştirin!");
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
