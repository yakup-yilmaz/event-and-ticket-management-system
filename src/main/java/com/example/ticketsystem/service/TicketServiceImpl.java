package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.TicketPurchaseRequest;
import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.entity.NotificationType;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.exception.BusinessException;
import com.example.ticketsystem.exception.ResourceNotFoundException;
import com.example.ticketsystem.mapper.TicketMapper;
import com.example.ticketsystem.repository.EventRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.repository.UserRepository;
import com.example.ticketsystem.security.SecurityUtils;
import com.example.ticketsystem.strategy.PricingStrategyFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final PricingStrategyFactory pricingStrategyFactory;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TicketResponse buyTicket(TicketPurchaseRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Bilet satın alma. User ID: {}, Event ID: {}", currentUserId, request.getEventId());

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanici bulunamadi ID: " + currentUserId));

        Event event = eventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + request.getEventId()));

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new BusinessException("EVENT_NOT_ACTIVE", "Bu etkinlik satis yapmak icin aktif durumda degil!");
        }

        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("EVENT_PASSED", "Tarihi geçmiş etkinlik için bilet satışı yapılamaz!");
        }

        if (event.getAvailableSeats() <= 0) {
            throw new BusinessException("EVENT_SOLD_OUT", "Mevcut etkinlikte bos koltuk kalmamistir!");
        }

        BigDecimal finalPrice = pricingStrategyFactory
                .getStrategy(event.getPricingType())
                .calculatePrice(event);

        Set<String> occupiedSeats = ticketRepository
            .findSeatNumbersByEventIdAndStatus(request.getEventId(), TicketStatus.PURCHASED);
        String assignedSeat = findFirstAvailableSeat(event.getTotalSeats(), occupiedSeats);

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        if (event.getAvailableSeats() == 0) {
            event.setStatus(EventStatus.SOLD_OUT);
        }
        eventRepository.save(event);

        if (event.getStatus() == EventStatus.SOLD_OUT) {
            notificationService.notifyFavoriters(
                    event,
                    NotificationType.STATUS_CHANGED,
                    String.format("'%s' etkinliğinde tüm koltuklar satıldı.", event.getName())
            );
        }

        Ticket ticket = Ticket.builder()
                .user(user)
                .event(event)
                .pricePaid(finalPrice)
                .seatNumber(assignedSeat)
                .ticketCode("TKT-" + UUID.randomUUID().toString().replace("-", "").toUpperCase())
                .status(TicketStatus.PURCHASED)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Bilet oluşturuldu. Code: {}, Koltuk: {}", savedTicket.getTicketCode(), assignedSeat);
        return ticketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bilet bulunamadi ID: " + id));
        assertTicketAccess(ticket.getUser().getId());
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Kendi biletlerim. User ID: {}", currentUserId);
        return ticketRepository.findByUserIdWithDetails(currentUserId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUserId(Long userId) {
        assertTicketAccess(userId);
        return ticketRepository.findByUserIdWithDetails(userId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithDetails(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Bilet bulunamadi ID: " + ticketId));

        assertTicketAccess(ticket.getUser().getId());

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessException("TICKET_ALREADY_CANCELLED", "Bu bilet zaten iptal edilmis!");
        }

        Event event = eventRepository.findByIdWithLock(ticket.getEvent().getId())
                .orElseThrow(() -> new BusinessException("EVENT_NOT_FOUND", "Etkinlik bulunamadı"));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("EVENT_CANCELLED", "İptal edilmiş etkinlikte bilet iptali yapılamaz!");
        }

        if (event.getStatus() == EventStatus.PASSED
                || !event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("EVENT_PASSED", "Geçmiş etkinlik bileti iptal edilemez!");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setSeatNumber(ticket.getSeatNumber() + "-CANCELLED-" + ticket.getId());
        ticketRepository.save(ticket);


        event.setAvailableSeats(event.getAvailableSeats() + 1);
        boolean stockBecameAvailable = event.getStatus() == EventStatus.SOLD_OUT;
        if (event.getStatus() == EventStatus.SOLD_OUT) {
            event.setStatus(EventStatus.ACTIVE);
        }
        eventRepository.save(event);

        if (stockBecameAvailable) {
            notificationService.notifyFavoriters(
                    event,
                    NotificationType.STOCK_AVAILABLE,
                    String.format("'%s' etkinliğinde yeniden bilet satışa açıldı.", event.getName())
            );
        }

        log.info("Bilet iptal edildi. Ticket ID: {}", ticketId);
    }

    /** Sahibi veya ADMIN erişebilir. */
    private void assertTicketAccess(Long ownerUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(ownerUserId) && !SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Bu bilete erişim yetkiniz yok");
        }
    }

    private String findFirstAvailableSeat(int totalSeats, Set<String> occupiedSeats) {
        for (int seatIndex = 1; seatIndex <= totalSeats; seatIndex++) {
            String seat = "KOLTUK-" + seatIndex;
            if (!occupiedSeats.contains(seat)) {
                return seat;
            }
        }
        throw new BusinessException("Mevcut etkinlikte bos koltuk kalmamistir!");
    }
}
