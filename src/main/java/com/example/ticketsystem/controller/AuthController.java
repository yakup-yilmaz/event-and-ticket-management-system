package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.AuthResponse;
import com.example.ticketsystem.dto.LoginRequest;
import com.example.ticketsystem.dto.MessageResponse;
import com.example.ticketsystem.dto.RefreshTokenRequest;
import com.example.ticketsystem.dto.UserCreateRequest;
import com.example.ticketsystem.dto.UserResponse;
import com.example.ticketsystem.service.AuthService;
import com.example.ticketsystem.security.LoginRateLimiter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        log.info("REST: Kayıt. Email: {}", request.getEmail());
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("REST: Giriş. Email: {}", request.getEmail());
        loginRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST: Access token yenileme");
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        log.info("REST: Çıkış");
        authService.logout(authorization, request != null ? request.getRefreshToken() : null);
        return ResponseEntity.ok(MessageResponse.builder()
            .message("Çıkış başarılı. Tokenlar geçersiz kılındı.")
                .build());
    }
}
