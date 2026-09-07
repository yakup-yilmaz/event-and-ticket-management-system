package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.EventCreateRequest;
import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.dto.EventUpdateRequest;
import com.example.ticketsystem.entity.EventStatus;
import java.util.List;

public interface EventService {

    EventResponse createEvent(EventCreateRequest request);

    EventResponse getEventById(Long id);

    List<EventResponse> getAllEvents();

    List<EventResponse> getEventsByStatus(EventStatus status);

    List<EventResponse> getActiveEvents();

    void deleteEvent(Long id);

    List<EventResponse> searchEventsByName(String keyword);

    EventResponse updateEvent(Long id, EventUpdateRequest request);

    EventResponse patchEvent(Long id, EventUpdateRequest request);
}
