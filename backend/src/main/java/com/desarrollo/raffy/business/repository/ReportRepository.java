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
    AND r.timestamp >= COALESCE(:start, r.timestamp)
    AND r.timestamp <= COALESCE(:end, r.timestamp)
    ORDER BY r.timestamp DESC
    """)
    List<Report> findAllReportFilter(
        @Param("status") StatusReport status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end    
    );

    boolean existsByEventIdAndMailUserReport(Long eventId, String mainUserReport);

    @Query("SELECT r FROM Report r WHERE r.eventId = :eventId")
    Optional<List<Report>> findbyeventId(@Param("eventId") Long eventId);
}
