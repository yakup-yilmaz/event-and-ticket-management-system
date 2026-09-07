package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.EventCreateRequest;
import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.dto.EventUpdateRequest;
import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.entity.NotificationType;
import com.example.ticketsystem.exception.ResourceNotFoundException;
import com.example.ticketsystem.mapper.EventMapper;
import com.example.ticketsystem.repository.EventRepository;
import com.example.ticketsystem.repository.FavoriteRepository;
import com.example.ticketsystem.repository.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final NotificationService notificationService;
    private final FavoriteRepository favoriteRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {
        log.info("Yeni etkinlik olusturma istegi alindi. Etkinlik Adi: {}, Kontenjan: {}",
                request.getName(), request.getTotalSeats());

        if (!request.getEventDate().isAfter(LocalDateTime.now())) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Etkinlik tarihi gelecekte olmalıdır");
        }

        Event event = eventMapper.toEntity(request);
        if (eventRepository.existsByNameAndEventDate(request.getName(), request.getEventDate())) {
            throw new com.example.ticketsystem.exception.BusinessException(
                "Aynı isim ve tarihte bir etkinlik zaten mevcut");
        }
        event.setAvailableSeats(request.getTotalSeats());
        event.setStatus(EventStatus.ACTIVE);
        if (request.getPricingType() == null) {
            event.setPricingType(com.example.ticketsystem.strategy.PricingType.STANDARD);
        } else {
            event.setPricingType(request.getPricingType());
        }

        Event savedEvent = eventRepository.save(event);
        log.info("Etkinlik basariyla olusturuldu. Event ID: {}, Status: {}",
                savedEvent.getId(), savedEvent.getStatus());

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        log.info("Etkinlik detaylari getiriliyor. Event ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + id));
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        log.info("Tüm etkinlikler getiriliyor");
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        log.info("Etkinlikler status ile getiriliyor: {}", status);
        return eventMapper.toResponseList(eventRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getActiveEvents() {
        return getEventsByStatus(EventStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Etkinlik silme istegi alindi. Event ID: {}", id);

        Event event = eventRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + id));

        if (ticketRepository.countByEventIdAndStatus(id, com.example.ticketsystem.entity.TicketStatus.PURCHASED) > 0) {
            throw new com.example.ticketsystem.exception.BusinessException(
            "Aktif bileti bulunan etkinlik silinemez. Etkinliği CANCELLED yapın.");
        }

        // İptal edilmiş bilet geçmişi varsa FK hatası vermemesi için önce temizle
        List<com.example.ticketsystem.entity.Ticket> cancelledTickets =
                ticketRepository.findByEventIdAndStatus(id, com.example.ticketsystem.entity.TicketStatus.CANCELLED);
        if (!cancelledTickets.isEmpty()) {
            ticketRepository.deleteAll(cancelledTickets);
        }

        notificationService.notifyFavoriters(
                event,
                NotificationType.EVENT_DELETED,
                String.format("'%s' etkinliği sistemden silindi.", event.getName())
        );
        favoriteRepository.deleteByEventId(id);
        eventRepository.delete(event);

        log.info("Etkinlik basariyla silindi. Event ID: {}", id);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        log.info("Etkinlik tam guncelleme (PUT) istegi alindi. Event ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + id));

        validateFullUpdate(request);

        Snapshot before = Snapshot.from(event);

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setBasePrice(request.getBasePrice());
        updateCapacity(event, request.getTotalSeats());
        event.setPricingType(request.getPricingType());
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        validateStatusTransition(before.status(), event);

        Event updatedEvent = eventRepository.save(event);
        publishChangeNotifications(before, updatedEvent);
        cancelTicketsIfEventCancelled(updatedEvent);
        log.info("Etkinlik basariyla guncellendi (PUT). Event ID: {}", updatedEvent.getId());
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse patchEvent(Long id, EventUpdateRequest request) {
        log.info("Etkinlik kismi guncelleme (PATCH) istegi alindi. Event ID: {}", id);

        if (request.getName() == null && request.getDescription() == null
                && request.getEventDate() == null && request.getBasePrice() == null
                && request.getTotalSeats() == null && request.getPricingType() == null
                && request.getStatus() == null) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "PATCH isteğinde en az bir alan belirtilmelidir");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + id));

        Snapshot before = Snapshot.from(event);

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (!request.getEventDate().isAfter(LocalDateTime.now())) {
                throw new com.example.ticketsystem.exception.BusinessException(
                        "Etkinlik tarihi gelecekte olmalıdır");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getBasePrice() != null) {
            event.setBasePrice(request.getBasePrice());
        }
        if (request.getTotalSeats() != null) {
            updateCapacity(event, request.getTotalSeats());
        }
        if (request.getPricingType() != null) {
            event.setPricingType(request.getPricingType());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        validateStatusTransition(before.status(), event);

        Event patchedEvent = eventRepository.save(event);
        publishChangeNotifications(before, patchedEvent);
        cancelTicketsIfEventCancelled(patchedEvent);
        log.info("Etkinlik basariyla kismi guncellendi (PATCH). Event ID: {}", patchedEvent.getId());
        return eventMapper.toResponse(patchedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> searchEventsByName(String keyword) {
        log.info("Etkinlikler aranıyor. Keyword: {}", keyword);
        return eventMapper.toResponseList(eventRepository.findByNameContainingIgnoreCase(keyword));
    }

    private void publishChangeNotifications(Snapshot before, Event after) {
        if (before.basePrice().compareTo(after.getBasePrice()) != 0) {
            notificationService.notifyFavoriters(
                    after,
                    NotificationType.PRICE_CHANGED,
                    String.format("'%s' etkinliğinin fiyatı %s → %s olarak güncellendi.",
                            after.getName(), before.basePrice(), after.getBasePrice())
            );
        }

        if (!Objects.equals(before.eventDate(), after.getEventDate())) {
            String message = String.format("'%s' etkinliğinin tarihi %s → %s olarak değişti.",
                after.getName(), before.eventDate(), after.getEventDate());
            notificationService.notifyFavoritersAndTicketHolders(
                after, NotificationType.DATE_CHANGED, message);
        }

        if (before.status() != after.getStatus()) {
            if (after.getStatus() == EventStatus.CANCELLED) {
            notificationService.notifyFavoritersAndTicketHolders(
                after,
                NotificationType.EVENT_CANCELLED,
                String.format("'%s' etkinliği iptal edildi. Bilet iadesi yapılacaktır.", after.getName())
            );
            } else {
                notificationService.notifyFavoriters(
                        after,
                        NotificationType.STATUS_CHANGED,
                        String.format("'%s' etkinliğinin durumu %s → %s oldu.",
                                after.getName(), before.status(), after.getStatus())
                );
            }
        }
    }

    private void cancelTicketsIfEventCancelled(Event event) {
        if (event.getStatus() != EventStatus.CANCELLED) {
            return;
        }

        List<com.example.ticketsystem.entity.Ticket> activeTickets =
                ticketRepository.findByEventIdAndStatus(
                        event.getId(), com.example.ticketsystem.entity.TicketStatus.PURCHASED);
        activeTickets.forEach(ticket -> ticket.setStatus(
                com.example.ticketsystem.entity.TicketStatus.CANCELLED));
        if (!activeTickets.isEmpty()) {
            ticketRepository.saveAll(activeTickets);
        }
    }

    private void validateFullUpdate(EventUpdateRequest request) {
        if (request.getName() == null || request.getDescription() == null
                || request.getEventDate() == null || request.getBasePrice() == null
                || request.getTotalSeats() == null || request.getPricingType() == null) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "PUT isteğinde tüm etkinlik alanları belirtilmelidir");
        }
    }

    private void updateCapacity(Event event, Integer newTotalSeats) {
        if (newTotalSeats > event.getTotalSeats()) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Etkinlik kapasitesi sonradan artırılamaz");
        }
        long soldSeats = ticketRepository.countByEventIdAndStatus(event.getId(),
            com.example.ticketsystem.entity.TicketStatus.PURCHASED);
        if (newTotalSeats < soldSeats) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Yeni kapasite satılmış koltuk sayısından düşük olamaz");
        }
        event.setTotalSeats(newTotalSeats);
        event.setAvailableSeats((int) (newTotalSeats - soldSeats));
        if (event.getAvailableSeats() == 0) {
            event.setStatus(EventStatus.SOLD_OUT);
        }
    }

    private void validateStatusTransition(EventStatus beforeStatus, Event event) {
        EventStatus afterStatus = event.getStatus();
        if (beforeStatus == afterStatus) {
            return;
        }
        if (beforeStatus == EventStatus.CANCELLED || beforeStatus == EventStatus.PASSED) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "CANCELLED veya PASSED etkinlik tekrar aktif hale getirilemez");
        }
        if (beforeStatus == EventStatus.SOLD_OUT && afterStatus == EventStatus.ACTIVE) {
            throw new com.example.ticketsystem.exception.BusinessException(
                "SOLD_OUT etkinlik yalnızca bilet iptali sonrası ACTIVE olabilir");
        }
        if (afterStatus == EventStatus.SOLD_OUT && event.getAvailableSeats() > 0) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Boş koltuk bulunan etkinlik SOLD_OUT yapılamaz");
        }
        if (afterStatus == EventStatus.ACTIVE && !event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Geçmiş tarihli etkinlik ACTIVE yapılamaz");
        }
        if (afterStatus == EventStatus.ACTIVE && event.getAvailableSeats() == 0) {
            throw new com.example.ticketsystem.exception.BusinessException(
                "Boş koltuk bulunmayan etkinlik ACTIVE yapılamaz");
        }
        if (afterStatus == EventStatus.PASSED && event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new com.example.ticketsystem.exception.BusinessException(
                    "Gelecek tarihli etkinlik PASSED yapılamaz");
        }
    }

    private record Snapshot(BigDecimal basePrice, LocalDateTime eventDate, EventStatus status) {
        static Snapshot from(Event event) {
            return new Snapshot(event.getBasePrice(), event.getEventDate(), event.getStatus());
        }
    }
}
