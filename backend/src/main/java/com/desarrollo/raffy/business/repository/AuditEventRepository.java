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
        WHERE a.creatorEvent = COALESCE(:creator, a.creatorEvent)
        AND a.type = COALESCE(:type, a.type)
        AND a.startDate >= COALESCE(:start, a.startDate)
        AND a.endDate <= COALESCE(:end, a.endDate)
        ORDER BY eventId DESC
        """)
    List<AuditEvent> getEventsByCreator(
        @Param("creator") String creatorEvent,
        @Param("type") EventTypes type,
        @Param("start") LocalDate startDate,
        @Param("end") LocalDate endDate
    );
 
    @Query("SELECT a FROM AuditEvent a WHERE a.relatedEventId = :id")
    Optional<AuditEvent> findByRelatedEventId(@Param("id") Long relatedEventId);
}
