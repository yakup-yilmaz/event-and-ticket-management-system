package com.example.ticketsystem.repository;


import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Yakup Yılmaz
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long>
{
    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByEventIdAndStatus(Long eventId, TicketStatus status);

    // Bir etkinliğe ait belirli durumdaki bilet sayısı
    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    long countByEventId(Long eventId);
    
    @Query("SELECT t FROM Ticket t JOIN FETCH t.user JOIN FETCH t.event WHERE t.id=:id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t JOIN FETCH t.user JOIN FETCH t.event WHERE t.id=:id")
    Optional<Ticket> findByIdWithLock(@Param("id") Long id);

    // P0-2 Fix: Kullanıcı biletlerini N+1 olmadan çekmek için JOIN FETCH
    @Query("SELECT t FROM Ticket t JOIN FETCH t.user JOIN FETCH t.event WHERE t.user.id = :userId")
    List<Ticket> findByUserIdWithDetails(@Param("userId") Long userId);

    // P0-3 Fix: Bildirim gönderiminde N+1 olmadan kullanıcıları çekmek için JOIN FETCH
    @Query("SELECT t FROM Ticket t JOIN FETCH t.user WHERE t.event.id = :eventId AND t.status = :status")
    List<Ticket> findByEventIdAndStatusWithUser(@Param("eventId") Long eventId, @Param("status") TicketStatus status);

    // P0-1 Fix: Koltuk kontrolü için sadece seat number çeken lightweight sorgu
    @Query("SELECT t.seatNumber FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    Set<String> findSeatNumbersByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") TicketStatus status);

}
