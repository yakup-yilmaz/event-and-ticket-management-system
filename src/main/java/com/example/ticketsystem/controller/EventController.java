package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.EventCreateRequest;
import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.dto.EventUpdateRequest;
import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        log.info("REST: Etkinlik oluşturma. Başlık: {}", request.getName());
        return new ResponseEntity<>(eventService.createEvent(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("REST: Tüm etkinlikler");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/active")
    public ResponseEntity<List<EventResponse>> getActiveEvents() {
        log.info("REST: Aktif etkinlikler");
        return ResponseEntity.ok(eventService.getActiveEvents());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(@PathVariable EventStatus status) {
        log.info("REST: Etkinlikler status={}", status);
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEventsByName(@RequestParam String keyword) {
        log.info("REST: Etkinlik arama. Keyword: {}", keyword);
        return ResponseEntity.ok(eventService.searchEventsByName(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        log.info("REST: Etkinlik detayı. ID: {}", id);
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        log.info("REST: Etkinlik PUT. ID: {}", id);
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EventResponse> patchEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        log.info("REST: Etkinlik PATCH. ID: {}", id);
        return ResponseEntity.ok(eventService.patchEvent(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("REST: Etkinlik silme. ID: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
