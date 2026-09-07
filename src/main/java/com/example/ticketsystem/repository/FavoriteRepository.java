package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Favorite> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.event WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithEvent(@Param("userId") Long userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.user WHERE f.event.id = :eventId")
    List<Favorite> findByEventIdWithUser(@Param("eventId") Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    void deleteByEventId(Long eventId);
}
