package com.desarrollo.raffy.business.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.AdminEventReportRepository;
import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ReportRepository;
import com.desarrollo.raffy.dto.report.AdminEventReportDTO;
import com.desarrollo.raffy.dto.report.AdminReportDTO;
import com.desarrollo.raffy.dto.report.CreateReportDTO;
import com.desarrollo.raffy.dto.report.CreateReportResponseDTO;
import com.desarrollo.raffy.dto.report.OrganizerEventReportsDTO;
import com.desarrollo.raffy.dto.report.ReviewReportDTO;
import com.desarrollo.raffy.exception.ResourceNotFoundException;
import com.desarrollo.raffy.model.AdminEventReport;
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
    private EmailService emailService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private AdminEventReportRepository adminEventReportRepository;

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
    public CreateReportResponseDTO createReport(CreateReportDTO dto){
        
        boolean alreadyExists = repository.existsByEventIdAndMailUserReport(
            dto.getEventId(), 
            dto.getMailUserReport());

        if(alreadyExists){
            throw new IllegalArgumentException("Ya has reportado este evento.");
        }

        if(dto.getEventId() == null){
            throw new IllegalArgumentException("Los IDs no pueden ser nulos.");
        }

        Events event = eventsRepository.findById(dto.getEventId())
        .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        if(dto.getMailUserReport().equals(event.getCreator().getEmail())){
            throw new IllegalArgumentException("No puedes reportar tu propio evento.");
        }

        Report report = new Report();
        report.setEvent(event);
        report.setMailUserReport(dto.getMailUserReport());
        report.setReason(dto.getReason());
        report.setStatusReport(StatusReport.EARRING);
        report.setCreateAt(LocalDateTime.now());

        Report saved = repository.save(report);

        // Enviar un mail al reportante
        emailService.sendEmailToCreateReporter(
            report.getMailUserReport(), 
            report.getEvent().getTitle(),
            "El reporte ha sido recibido y está pendiente de revisión.");

        // Crear o actualizar AdminEventReport
        createOrUpdateAdminEventReport(event.getId());

        return new CreateReportResponseDTO(
            saved.getEvent().getTitle(),
            saved.getStatusReport(),
            saved.getCreateAt()
        );
    }

    /**
     * Crea o actualiza AdminEventReport para un evento
     */
    @Transactional
    public void createOrUpdateAdminEventReport(Long eventId) {
        if(eventId == null){
            throw new IllegalArgumentException("El ID del evento no puede ser nulo.");
        }

        Events event = eventsRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        int totalReports = repository.countByEventId(eventId);
        int score = scoreService.calculateScoreEvent(eventId);

        // Buscar si ya existe
        AdminEventReport adminReport = adminEventReportRepository.findByEventId(eventId)
            .orElse(null);

        if(adminReport == null){
            // Crear nuevo
            adminReport = new AdminEventReport();
            adminReport.setEvent(event);
            adminReport.setStatusReport(StatusReport.EARRING);
            adminReport.setCreatedAt(LocalDateTime.now());
        } else {
            // Actualizar existente
            adminReport.setUpdatedAt(LocalDateTime.now());
        }

        adminReport.setTotalReports(totalReports);
        adminReport.setScore(score);

        adminEventReportRepository.save(adminReport);
    }

    /**
     * Obtener todos los reportes
     * @return
     */
    @Transactional(readOnly = true)
    public List<AdminEventReportDTO> getAllEventsWithReportsSummary(){
        List<AdminEventReport> adminReports = adminEventReportRepository.findPendingReports();
        return adminReports.stream()
        .map(this::convertToDTO)
        .toList();
    }

    /**
     * Obtiene detalles completos de un evento reportado
     */
    @Transactional(readOnly = true)
    public AdminEventReportDTO getEventReportDetails(Long eventId){
        if(eventId == null){
            throw new IllegalArgumentException("El ID del evento no puede ser nulo.");
        }

        AdminEventReport adminReport = adminEventReportRepository.findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Este evento no tiene reportes administrativos."));

        Events event = adminReport.getEvent();
        List<ReviewReportDTO> reportDTOs = getAllReportEventId(eventId);

        AdminEventReportDTO dto = new AdminEventReportDTO();
        dto.setId(adminReport.getId());
        dto.setEventId(event.getId());
        dto.setEventTitle(event.getTitle());
        dto.setEventDate(event.getStartDate());
        dto.setStatusEvent(event.getStatusEvent());
        dto.setStatusReport(adminReport.getStatusReport());
        dto.setTotalReports(reportDTOs.size());
        dto.setScore(adminReport.getScore());
        dto.setReports(reportDTOs);
        dto.setCreatedAt(adminReport.getCreatedAt());
        dto.setUpdatedAt(adminReport.getUpdatedAt());

        return dto;
    }

    /**
     * Obtiene todos los reportes de usuarios para un evento
     */
    private List<ReviewReportDTO> getAllReportEventId(Long eventId){
        List<Report> reports = repository
        .findbyeventId(eventId).orElseThrow(() -> new IllegalArgumentException("No se encontraron reportes para este evento."));
        List<ReviewReportDTO> reportDTOs = new ArrayList<>();
        for(Report r: reports){
            ReviewReportDTO adminReport = new ReviewReportDTO(
                r.getId(),
                r.getMailUserReport(),
                r.getReason(),
                r.getStatusReport(),
                r.getCreateAt()
            );
            reportDTOs.add(adminReport);
        }
        return reportDTOs;
    }

    /**
     * Método que rechaza o aprueba la solicitud
     * @return
     */
    @Transactional
    public ReviewReportDTO reviewReport(Long reportId, StatusReport status) {
        Report report = repository.findById(reportId)
        .orElseThrow(() -> new IllegalArgumentException("No se encontro el reporte."));

        if(status != StatusReport.APPROVED && status != StatusReport.REJECTED){
            throw new IllegalArgumentException("Estado inválido para revisión del reporte.");
        }
        report.setStatusReport(status);
        Report saved = repository.save(report);

        //AGREGAR: Mandar un mail sobre su reporte

        return new ReviewReportDTO(
            saved.getId(),
            saved.getMailUserReport(),
            saved.getReason(),
            saved.getStatusReport(),
            saved.getCreateAt()
        );
    }

    /**
     * Método que notifica al creador del evento sobre los reportes recibidos.
     * @param eventId
     * @param recommendation
     */
    @Transactional
    public void notifyCreatorAboutReports(Long eventId, String adminMessage){
        if(eventId == null){
            throw new IllegalArgumentException("El ID del evento no puede ser nulo.");
        }

        if(adminMessage == null || adminMessage.isBlank()) {
            throw new IllegalArgumentException("El mensaje del administrador no puede estar vacío.");
        }

        Events event = eventsRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        AdminEventReport adminReport = adminEventReportRepository
            .findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("No hay reportes para este evento."));
        
        int totalReports = repository.countByEventId(eventId);

        
            emailService.sendEventReportNotificationToCreator(
                event.getCreator().getEmail(), 
                event.getCreator().getName() + " " + event.getCreator().getSurname(), 
                eventId, 
                event.getTitle(), 
                totalReports, 
                adminMessage
            );

        adminReport.setAdminNotes(adminMessage);
        adminReport.setUpdatedAt(LocalDateTime.now());
        adminEventReportRepository.save(adminReport);
    }

    /**
     * Método para Definir si el evento sigue activo o se bloquea
     * @param eventId
     * @param statusReport
     * @return
     */
    @Transactional
    public OrganizerEventReportsDTO makeFinalDecisionOnEvent(Long eventId, StatusReport finalStatusReport, String AdminMessage) {
        
        if(eventId == null){
            throw new IllegalArgumentException("El ID del evento no puede ser nulo.");
        }

        if(finalStatusReport != StatusReport.APPROVED && finalStatusReport != StatusReport.REJECTED){
            throw new IllegalArgumentException("El estado debe ser APPROVED o REJECTED.");
        }

        AdminEventReport adminReport = adminEventReportRepository
            .findByEventId(eventId)
            .orElseThrow(() ->  new ResourceNotFoundException("No hay reportes administrativos para este evento."));

        Events event = adminReport.getEvent();

        adminReport.setStatusReport(finalStatusReport);
        adminReport.setAdminNotes(AdminMessage);
        adminReport.setUpdatedAt(LocalDateTime.now());
        adminEventReportRepository.save(adminReport);

        if (finalStatusReport == StatusReport.APPROVED) {
            event.setStatusEvent(StatusEvent.BLOCKED);
        } else {
            event.setStatusEvent(StatusEvent.OPEN);
        }

        eventsRepository.save(event);

        String message = finalStatusReport == StatusReport.APPROVED
            ? "Tu evento ha sido bloqueado debido a múltiples reportes verificados."
            : "Los reportes sobre tu evento han sido revisados y rechazados. Tu evento permanece activo.";

        emailService.sendFinalDecisionNotification(
            event.getCreator().getEmail(),
            event.getCreator().getName() + " " + event.getCreator().getSurname(),
            event.getTitle(),
            finalStatusReport,
            message
        );

        // AGREGAR: Mandar un mail a los usuarios que reportaron
        
        OrganizerEventReportsDTO dto = new OrganizerEventReportsDTO();
        dto.setEventId(event.getId());
        dto.setEventTitle(event.getTitle());
        dto.setStatusEvent(event.getStatusEvent());

        return dto;
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

    /**
     * Convierte AdminEventReport a DTO
     */
    private AdminEventReportDTO convertToDTO(AdminEventReport adminReport){
        AdminEventReportDTO dto = new AdminEventReportDTO();
        dto.setId(adminReport.getId());
        dto.setEventId(adminReport.getEvent().getId());
        dto.setEventTitle(adminReport.getEvent().getTitle());
        dto.setEventDate(adminReport.getEvent().getStartDate());
        dto.setStatusEvent(adminReport.getEvent().getStatusEvent());
        dto.setStatusReport(adminReport.getStatusReport());
        dto.setTotalReports(adminReport.getTotalReports());
        dto.setScore(adminReport.getScore());
        dto.setCreatedAt(adminReport.getCreatedAt());
        dto.setUpdatedAt(adminReport.getUpdatedAt());
        return dto;
    }
}
