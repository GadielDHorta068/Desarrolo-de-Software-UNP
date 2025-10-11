package com.desarrollo.raffy.business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.AuditLog;
import com.desarrollo.raffy.model.AuditParticipant;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLog, Long>{
    
    @Query("SELECT a FROM AuditLog a WHERE a.creatorNickname = :nickname")
    Optional<List<AuditLog>> getAuditLogByCreator(@Param("nickname") String nickname);

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
