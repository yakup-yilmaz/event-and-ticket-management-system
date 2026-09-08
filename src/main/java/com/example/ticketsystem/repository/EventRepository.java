package com.example.ticketsystem.repository;


import com.example.ticketsystem.entity.Event;
import com.example.ticketsystem.entity.EventStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
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
public interface EventRepository extends JpaRepository<Event, Long>
{
    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndEventDateBefore(EventStatus status, LocalDateTime dateTime);

    List<Event> findByStatusInAndEventDateBefore(List<EventStatus> statuses, LocalDateTime dateTime);
    
    List<Event> findByNameContainingIgnoreCase(String keyword);
    
    boolean existsByNameAndEventDate(String name, java.time.LocalDateTime eventDate);
    
    boolean existsByNameAndEventDateAndIdNot(String name, java.time.LocalDateTime eventDate, Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id=:id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);
            
}
