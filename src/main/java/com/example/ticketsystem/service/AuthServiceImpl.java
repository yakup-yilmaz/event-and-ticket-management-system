package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.AuthResponse;
import com.example.ticketsystem.dto.LoginRequest;
import com.example.ticketsystem.dto.RefreshTokenRequest;
import com.example.ticketsystem.dto.UserCreateRequest;
import com.example.ticketsystem.dto.UserResponse;
import com.example.ticketsystem.entity.RefreshToken;
import com.example.ticketsystem.entity.Role;
import com.example.ticketsystem.entity.TokenBlacklist;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.exception.BusinessException;
import com.example.ticketsystem.mapper.UserMapper;
import com.example.ticketsystem.repository.RefreshTokenRepository;
import com.example.ticketsystem.repository.TokenBlacklistRepository;
import com.example.ticketsystem.repository.UserRepository;
import com.example.ticketsystem.security.JwtService;
import com.example.ticketsystem.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        log.info("Kayıt isteği alındı");

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("DUPLICATE_EMAIL", "Bu email adresi ile kayitli bir kullanici zaten var!");
        }

        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        log.info("Kullanıcı kaydedildi. ID: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Geçersiz kimlik bilgileri"));

        // Süresi dolmuş refresh kayıtlarını temizle (geçerli olanlara dokunulmaz)
        refreshTokenRepository.deleteExpiredByUser(user, LocalDateTime.now());

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = createAndStoreRefreshToken(user);

        log.info("Giriş başarılı. User ID: {}", user.getId());
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Geçersiz refresh token"));

        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllActiveByUser(stored.getUser(), LocalDateTime.now());
            log.warn("Revoked refresh token kullanımı tespit edildi (Güvenlik İhlali). User ID: {}",
                    stored.getUser().getId());
            throw new BusinessException("TOKEN_REUSE_DETECTED", "Güvenlik ihlali tespit edildi, tüm oturumlarınız kapatıldı. Lütfen tekrar giriş yapın.");
        }
        
        if (stored.isExpired()) {
            throw new BusinessException("REFRESH_TOKEN_EXPIRED", "Refresh token süresi dolmuş. Lütfen tekrar giriş yapın.");
        }

        User user = stored.getUser();
        UserPrincipal principal = UserPrincipal.from(user);
        String newAccessToken = jwtService.generateAccessToken(principal);
        stored.setRevoked(true);
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);
        String newRefreshToken = createAndStoreRefreshToken(user);

        log.info("Access token yenilendi. User ID: {}", user.getId());
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException("ACCESS_TOKEN_REQUIRED", "Access token gerekli");
        }

        String raw = accessToken.startsWith("Bearer ")
                ? accessToken.substring(7)
                : accessToken;

        String jti = null;
        LocalDateTime expiresAt = null;

        try {
            jti = jwtService.extractJti(raw);
            expiresAt = jwtService.extractExpiration(raw);
        } catch (ExpiredJwtException ex) {
            jti = ex.getClaims().getId();
            expiresAt = ex.getClaims().getExpiration().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ex) {
            throw new BusinessException("INVALID_ACCESS_TOKEN", "Geçersiz access token");
        }

        if (jti != null && expiresAt != null) {
            if (!tokenBlacklistRepository.existsByJti(jti)) {
                try {
                    tokenBlacklistRepository.save(TokenBlacklist.builder()
                            .jti(jti)
                            .expiresAt(expiresAt)
                            .build());
                    log.info("Access token kara listeye alındı. jti={}", jti);
                } catch (DataIntegrityViolationException ignored) {
                    log.debug("Access token zaten kara listede. jti={}", jti);
                }
            }
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            String tokenHash = hashToken(refreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(stored -> {
                Long currentUserId = com.example.ticketsystem.security.SecurityUtils.getCurrentUserId();
                if (!stored.getUser().getId().equals(currentUserId)) {
                    log.warn("Yetkisiz refresh token logout denemesi! CurrentUser: {}, TokenOwner: {}",
                            currentUserId, stored.getUser().getId());
                    throw new BusinessException("ACCESS_DENIED", "Başkasına ait refresh token iptal edilemez!");
                }
                stored.setRevoked(true);
                stored.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(stored);
                log.info("Refresh token iptal edildi (Logout). User ID: {}", stored.getUser().getId());
            });
        }
    }

    private String createAndStoreRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(expiresAt)
                .build());

        return rawToken;
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algoritması bulunamadı", e);
        }
    }
}
