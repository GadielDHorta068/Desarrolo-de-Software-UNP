package com.desarrollo.raffy.business.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.auditlog.AuditEvent;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>{
    @Query("""
        SELECT a FROM AuditEvent a
        WHERE (:creator LOWER(a.creatorEvent) LIKE LOWER(CONCAT('%', :creator, '%')))
        AND (:type IS NULL OR a.eventType = :type)
        AND (:start IS NULL OR a.startDate >= :start)
        AND (:end IS NULL OR a.endDate <= :end)
        ORDER BY eventId DESC
        """)
    List<AuditEvent> getEventsByCreator(
        @Param("creator") String creatorEvent,
        @Param("type") EventTypes type,
        @Param("start") LocalDate startDate,
        @Param("end") LocalDate endDate
    );
 
    @Query("SELECT a FROM AuditEvent a WHERE a.eventId = :id")
    Optional<AuditEvent> getAuditEventById(@Param("id") Long eventId);
}
