package com.desarrollo.raffy.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ReportRepository;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Report;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.StatusReport;

@Service
public class ReportService {
    
    @Autowired
    private ReportRepository repository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    @Transactional
    public Report save(Report report){
        return repository.save(report);
    }

    /**
     * Crea el reporte asignando la fecha actual y en estado PENDIENTE
     * @param report
     * @return
     */
    @Transactional
    public Report createReport(Report report){
        
        boolean alreadyExists = repository.existsByEventIdAndMailUserReport(
            report.getEventId(), 
            report.getMailUserReport());

        if(alreadyExists){
            throw new IllegalArgumentException("Ya has reportado este evento.");
        }

        report.setStatusReport(StatusReport.EARRING);
        report.setTimestamp(LocalDateTime.now());

        return save(report);
    }

    /**
     * Filtrar los reportes por estado y fecha y hora
     * @return
     */
    @Transactional(readOnly = true)
    public List<Report> getAllReportFilter(StatusReport status, LocalDateTime start, LocalDateTime end){
        return repository.findAllReportFilter(status, start, end);
    }

    /**
     * Método que rechaza o aprueba la solicitud
     * @return
     */
    @Transactional
    public Report reviewReport(Long reportId, Long eventId, StatusReport status) {
        Report report = repository.findById(reportId)
        .orElseThrow(() -> new IllegalArgumentException("No se encontro el reporte."));

        Events events = eventsRepository.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("No se encontro el evento"));

        if (status == StatusReport.APPROVED) {
            report.setStatusReport(StatusReport.APPROVED);
            events.setStatusEvent(StatusEvent.BLOCKED); // Bloqueamos el eventos
        } else if(status == StatusReport.REJECTED) {
            report.setStatusReport(StatusReport.REJECTED);
        } else{
            throw new IllegalArgumentException("Estado inválido para revisión del reporte.");
        }

        eventsRepository.save(events);

        return repository.save(report);
    }

    /**
     * Método que consulta si el usuario reportó el evento
     * @param eventId
     * @param userMail
     * @return
     */
    @Transactional(readOnly = true)
    public boolean hasUserReportEvent(Long eventId, String userMail){
        return repository.existsByEventIdAndMailUserReport(eventId, userMail);
    }
}
