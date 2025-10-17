package com.desarrollo.raffy.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import com.desarrollo.raffy.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.remitenteId = :userId1 AND m.destinatarioId = :userId2) OR (m.remitenteId = :userId2 AND m.destinatarioId = :userId1) ORDER BY m.fechaEnvio ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.destinatarioId = :recipientId AND m.leido = false")
    long countUnread(@Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE Message m SET m.leido = true WHERE m.destinatarioId = :recipientId AND m.remitenteId = :senderId AND m.leido = false")
    int markAsRead(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);
}