package com.desarrollo.raffy.business.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.Report;
import com.desarrollo.raffy.model.StatusReport;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>{
  
    @Query("""
    SELECT r FROM Report r        
    WHERE r.statusReport = COALESCE(:status, r.statusReport)
    AND r.createAt >= COALESCE(:start, r.createAt)
    AND r.createAt <= COALESCE(:end, r.createAt)
    ORDER BY r.createAt DESC
    """)
    List<Report> findAllReportFilter(
        @Param("status") StatusReport status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end    
    );

    boolean existsByEventIdAndMailUserReport(Long eventId, String mainUserReport);

    @Query("SELECT r FROM Report r WHERE r.event.id = :eventId")
    Optional<List<Report>> findbyeventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.event.id = :eventId")
    int countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.event.creator.id = :creatorId")
    int countAllEventId(@Param("creatorId") Long creatorId);

   /*  @Query("""
    SELECT new com.desarrollo.raffy.dto.report.AdminEventReportDTO(
        e.id,
        e.title,
        e.startDate,
        e.statusEvent,
        COUNT(r)
    )
    FROM Events e
    LEFT JOIN Report r ON r.event.id = e.id
    WHERE e.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN
    GROUP BY e.id, e.title, e.startDate, e.statusEvent
    HAVING COUNT(r) > 0
    ORDER BY COUNT(r) DESC
    """)
    List<AdminEventReportDTO> findAllEventsWithReportSummary(); */

    @Query("""
    SELECT COUNT(r) FROM Report r 
    WHERE r.event.creator.id = :creatorId 
    AND r.event.statusEvent = com.desarrollo.raffy.model.StatusEvent.BLOCKED
    """)
    int countTotalReportsByCreatorBlockedEvents(@Param("creatorId") Long creatorId);
}
