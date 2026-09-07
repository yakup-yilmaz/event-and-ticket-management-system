package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.Favorite;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.exception.BusinessException;
import com.example.ticketsystem.exception.ResourceNotFoundException;
import com.example.ticketsystem.mapper.EventMapper;
import com.example.ticketsystem.repository.EventRepository;
import com.example.ticketsystem.repository.FavoriteRepository;
import com.example.ticketsystem.repository.UserRepository;
import com.example.ticketsystem.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse addFavorite(Long eventId) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (favoriteRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new BusinessException("Bu etkinlik zaten favorilerinizde");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadi ID: " + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanici bulunamadi ID: " + userId));

        try {
            favoriteRepository.save(Favorite.builder()
                    .user(user)
                    .event(event)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Bu etkinlik zaten favorilerinizde");
        }

        log.info("Favori eklendi. User ID: {}, Event ID: {}", userId, eventId);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void removeFavorite(Long eventId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Favorite favorite = favoriteRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Favori bulunamadi"));
        favoriteRepository.delete(favorite);
        log.info("Favori silindi. User ID: {}, Event ID: {}", userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getMyFavorites() {
        Long userId = SecurityUtils.getCurrentUserId();
        return favoriteRepository.findByUserIdWithEvent(userId).stream()
                .map(Favorite::getEvent)
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long eventId) {
        return favoriteRepository.existsByUserIdAndEventId(SecurityUtils.getCurrentUserId(), eventId);
    }
}
