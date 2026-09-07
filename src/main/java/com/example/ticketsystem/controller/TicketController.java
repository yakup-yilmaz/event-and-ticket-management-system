package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.TicketPurchaseRequest;
import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.service.TicketService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<TicketResponse> buyTicket(@Valid @RequestBody TicketPurchaseRequest request) {
        log.info("REST: Bilet satın alma. Event ID: {}", request.getEventId());
        return new ResponseEntity<>(ticketService.buyTicket(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        log.info("REST: Kendi biletlerim (/my)");
        return ResponseEntity.ok(ticketService.getMyTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        log.info("REST: Bilet detayı. ID: {}", id);
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByUserId(@PathVariable Long userId) {
        log.info("REST: Kullanıcı biletleri (ADMIN). User ID: {}", userId);
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTicket(@PathVariable Long id) {
        log.info("REST: Bilet iptal. Ticket ID: {}", id);
        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }
}
