package com.example.ticketsystem.dto;

import com.example.ticketsystem.entity.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String message;
    private boolean read;
    private Long eventId;
    private String eventName;
    private LocalDateTime createdAt;
}
