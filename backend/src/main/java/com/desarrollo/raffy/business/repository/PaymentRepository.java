package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Payment;

import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.eventId = :eventId AND p.receiverId = :receiverId")
    Payment findByEventIdAndReceiverId(@Param("eventId") Long eventId, @Param("receiverId") Long receiverId);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.eventId = :eventId")
    Payment findByUserIdAndEventId(@Param("userId") String userId, @Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.eventId = :eventId")
    Payment findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT p FROM Payment p WHERE p.receiverId = :receiverId")
    Payment findByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId")
    Payment findByUserId(@Param("userId") String userId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Payment findByStatus(@Param("status") String status);
}
