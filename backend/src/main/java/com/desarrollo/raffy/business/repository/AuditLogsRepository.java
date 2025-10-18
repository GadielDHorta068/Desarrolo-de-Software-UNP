package com.desarrollo.raffy.business.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.auditlog.AuditLog;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLog, Long>{
    
    @Query("SELECT a FROM AuditLog a WHERE a.creatorNickname = :nickname")
    Optional<List<AuditLog>> getAuditLogByCreator(@Param("nickname") String nickname);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.creatorNickname = :nickname
        AND (:title IS NULL OR LOWER(a.eventTitle) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:type IS NULL OR a.eventType = :type)
        AND (:from IS NULL OR a.executeDate >= :from)
        AND (:to IS NULL OR a.executeDate <= :to)
        ORDER BY a.executeDate DESC
        """)
    Optional<List<AuditLog>> getAuditLogByCreator(
      @Param("nickname") String nickname, 
      @Param("title") String title,
      @Param("type") EventTypes type,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

    @Query("""
    SELECT p 
    FROM AuditLog a 
    JOIN a.participants p 
    WHERE a.eventId = :eventId 
      AND p.userPosition <> 0
    ORDER BY p.userPosition ASC
    """)
    List<AuditParticipant> getAuditLogWinnersByEventId(@Param("eventId") Long eventId);

}
