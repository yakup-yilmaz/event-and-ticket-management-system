package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.NotificationResponse;
import com.example.ticketsystem.dto.UnreadCountResponse;
import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.Favorite;
import com.example.ticketsystem.entity.Notification;
import com.example.ticketsystem.entity.NotificationType;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.exception.ResourceNotFoundException;
import com.example.ticketsystem.repository.FavoriteRepository;
import com.example.ticketsystem.repository.NotificationRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.security.SecurityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public void notifyFavoriters(Event event, NotificationType type, String message) {
        List<Favorite> favorites = favoriteRepository.findByEventIdWithUser(event.getId());
        saveNotifications(event, type, message, favorites.stream()
                .map(Favorite::getUser)
                .toList());
    }

    @Override
    @Transactional
    public void notifyFavoritersAndTicketHolders(Event event, NotificationType type, String message) {
        Map<Long, User> recipients = new LinkedHashMap<>();

        favoriteRepository.findByEventIdWithUser(event.getId())
                .forEach(favorite -> recipients.put(favorite.getUser().getId(), favorite.getUser()));

        List<Ticket> tickets = ticketRepository.findByEventIdAndStatusWithUser(event.getId(), TicketStatus.PURCHASED);
        tickets.forEach(ticket -> recipients.put(ticket.getUser().getId(), ticket.getUser()));

        saveNotifications(event, type, message, new ArrayList<>(recipients.values()));
    }

    private void saveNotifications(Event event, NotificationType type, String message, List<User> users) {
        if (users.isEmpty()) {
            return;
        }

        List<Notification> batch = new ArrayList<>(users.size());
        for (User user : users) {
            batch.add(Notification.builder()
                    .user(user)
                    .event(event)
                    .type(type)
                    .message(message)
                    .read(false)
                    .build());
        }
        notificationRepository.saveAll(batch);
        log.info("Bildirim gönderildi. Event ID: {}, type: {}, alıcı: {}",
                event.getId(), type, batch.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findPageByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyUnreadNotifications(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findUnreadPageByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return UnreadCountResponse.builder()
                .unreadCount(notificationRepository.countByUserIdAndReadFalse(userId))
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadi ID: " + notificationId));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.markAllReadByUserId(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .read(n.isRead())
                .eventId(n.getEvent() != null ? n.getEvent().getId() : null)
                .eventName(n.getEvent() != null ? n.getEvent().getName() : null)
                .createdAt(n.getCreatedAt())
                .build();
}
    
}
