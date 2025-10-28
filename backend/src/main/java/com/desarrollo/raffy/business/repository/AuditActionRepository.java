package com.desarrollo.raffy.business.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.auditlog.AuditAction;
import com.desarrollo.raffy.model.auditlog.AuditActionType;

@Repository
public interface AuditActionRepository extends JpaRepository<AuditAction, Long>{
    
    @Query("""
        SELECT a FROM AuditAction a
        WHERE a.event.eventId = :eventId
        AND a.action = COALESCE(:action, a.action)
        AND a.timestamp >= COALESCE(:from, a.timestamp)
        AND a.timestamp <= COALESCE(:to, a.timestamp)
        ORDER BY a.timestamp DESC
        """)
    List<AuditAction> getByFiltersAction(
      @Param("eventId") Long eventId,
      @Param("action") AuditActionType action,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to
    );
}
