package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Payment;

import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Métodos que devuelven múltiples resultados
    @Query("SELECT p FROM Payment p WHERE p.event.id = :eventId")
    List<Payment> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.receiver.id = :receiverId")
    List<Payment> findAllByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    List<Payment> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findAllByStatus(@Param("status") String status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.event.id = :eventId")
    List<Payment> findAllByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.event.id = :eventId AND p.receiver.id = :receiverId")
    List<Payment> findAllByEventIdAndReceiverId(@Param("eventId") Long eventId, @Param("receiverId") Long receiverId);

    // Métodos que mantienen el comportamiento original (para compatibilidad)
    @Query("SELECT p FROM Payment p WHERE p.event.id = :eventId AND p.receiver.id = :receiverId")
    Payment findByEventIdAndReceiverId(@Param("eventId") Long eventId, @Param("receiverId") Long receiverId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.event.id = :eventId")
    Payment findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.event.id = :eventId")
    Payment findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.receiver.id = :receiverId")
    Payment findByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    Payment findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Payment findByStatus(@Param("status") String status);
}
