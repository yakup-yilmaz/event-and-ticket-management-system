package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.UserProfileUpdateRequest;
import com.example.ticketsystem.dto.UserResponse;
import com.example.ticketsystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /me → giriş yapmış herkes (kendi profili)
 * liste / {id} → yalnızca ADMIN
 * Kayıt: POST /api/v1/auth/register
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("REST: Kendi profil (/me)");
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        log.info("REST: Profil güncelleme (/me)");
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("REST: Tüm kullanıcılar (ADMIN)");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("REST: Kullanıcı detayı (ADMIN). ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
