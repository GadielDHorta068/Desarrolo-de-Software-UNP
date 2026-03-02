package com.desarrollo.raffy.business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.AdminEventReport;
import com.desarrollo.raffy.model.StatusReport;

@Repository
public interface AdminEventReportRepository extends JpaRepository<AdminEventReport, Long> {
    
    @Query("SELECT aer FROM AdminEventReport aer WHERE aer.event.id = :eventId")
    Optional<AdminEventReport> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT aer FROM AdminEventReport aer WHERE aer.statusReport = :status ORDER BY aer.createdAt DESC")
    List<AdminEventReport> findByStatus(@Param("status") StatusReport status);

    @Query("""
    SELECT aer FROM AdminEventReport aer 
    WHERE aer.event.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN
    AND aer.statusReport = com.desarrollo.raffy.model.StatusReport.EARRING
    ORDER BY aer.totalReports DESC, aer.createdAt DESC
    """)
    List<AdminEventReport> findPendingReports();

    boolean existsByEventId(Long eventId);
}
