package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.EventResponse;
import java.util.List;

public interface FavoriteService {

    EventResponse addFavorite(Long eventId);

    void removeFavorite(Long eventId);

    List<EventResponse> getMyFavorites();

    boolean isFavorite(Long eventId);
}
