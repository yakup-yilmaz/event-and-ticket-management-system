package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.service.FavoriteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{eventId}")
    public ResponseEntity<EventResponse> addFavorite(@PathVariable Long eventId) {
        log.info("REST: Favori ekle. Event ID: {}", eventId);
        return new ResponseEntity<>(favoriteService.addFavorite(eventId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long eventId) {
        log.info("REST: Favori sil. Event ID: {}", eventId);
        favoriteService.removeFavorite(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getMyFavorites() {
        log.info("REST: Favori listem");
        return ResponseEntity.ok(favoriteService.getMyFavorites());
    }

    @GetMapping("/{eventId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable Long eventId) {
        return ResponseEntity.ok(Map.of("favorite", favoriteService.isFavorite(eventId)));
    }
}
