package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.MessageResponse;
import com.example.ticketsystem.dto.NotificationResponse;
import com.example.ticketsystem.dto.UnreadCountResponse;
import com.example.ticketsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    private static final int MAX_PAGE_SIZE = 50;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("REST: Bildirimlerim");
        return ResponseEntity.ok(notificationService.getMyNotifications(clampPageable(pageable)));
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnread(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("REST: Okunmamış bildirimler");
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(clampPageable(pageable)));
    }

    private Pageable clampPageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("REST: Bildirim okundu. ID: {}", id);
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(MessageResponse.builder()
                .message(count + " bildirim okundu olarak işaretlendi")
                .build());
    }
}
