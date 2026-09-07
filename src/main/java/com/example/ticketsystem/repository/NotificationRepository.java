package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.event "
            + "WHERE n.user.id = :userId",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.event "
            + "WHERE n.user.id = :userId AND n.read = false",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    Page<Notification> findUnreadPageByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllReadByUserId(@Param("userId") Long userId);
}
