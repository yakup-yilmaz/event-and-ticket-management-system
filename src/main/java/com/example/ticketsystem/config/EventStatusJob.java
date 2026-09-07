package com.example.ticketsystem.config;

import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventStatusJob {

    private final EventRepository eventRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void markPassedEvents() {
        List<Event> expiredEvents = eventRepository.findByStatusInAndEventDateBefore(
            Arrays.asList(EventStatus.ACTIVE, EventStatus.SOLD_OUT), LocalDateTime.now());
        if (expiredEvents.isEmpty()) {
            return;
        }

        expiredEvents.forEach(event -> event.setStatus(EventStatus.PASSED));
        eventRepository.saveAll(expiredEvents);
        log.info("Süresi geçen etkinlikler PASSED olarak güncellendi. Adet: {}", expiredEvents.size());
    }
}