package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Bilet satın alma isteği.
 * userId kasıtlı olarak yok — kimlik JWT Principal'dan alınır (spoof önlenir).
 */
@Data
public class TicketPurchaseRequest {

    @NotNull(message = "Etkinlik ID boş olamaz")
    private Long eventId;
}
