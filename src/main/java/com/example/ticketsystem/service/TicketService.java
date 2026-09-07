package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.TicketPurchaseRequest;
import com.example.ticketsystem.dto.TicketResponse;
import java.util.List;

public interface TicketService {

    TicketResponse buyTicket(TicketPurchaseRequest request);

    TicketResponse getTicketById(Long id);

    /** JWT sahibi kullanıcının kendi biletleri. */
    List<TicketResponse> getMyTickets();

    List<TicketResponse> getTicketsByUserId(Long userId);

    void cancelTicket(Long ticketId);
}
