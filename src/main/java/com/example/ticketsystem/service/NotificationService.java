package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.NotificationResponse;
import com.example.ticketsystem.dto.UnreadCountResponse;
import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /** Etkinliği favorileyen her kullanıcıya ayrı bildirim satırı yazar. */
    void notifyFavoriters(Event event, NotificationType type, String message);

    /** Favoriler ve satın alınmış bilet sahiplerine kullanıcı başına tek bildirim yazar. */
    void notifyFavoritersAndTicketHolders(Event event, NotificationType type, String message);

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    Page<NotificationResponse> getMyUnreadNotifications(Pageable pageable);

    UnreadCountResponse getUnreadCount();

    NotificationResponse markAsRead(Long notificationId);

    int markAllAsRead();
}
