package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.ReportService;
import com.desarrollo.raffy.dto.report.AdminEventReportDTO;
import com.desarrollo.raffy.dto.report.CreateReportDTO;
import com.desarrollo.raffy.dto.report.CreateReportResponseDTO;
import com.desarrollo.raffy.dto.report.MessagerResponseDTO;
import com.desarrollo.raffy.dto.report.OrganizerEventReportsDTO;
import com.desarrollo.raffy.dto.report.ReviewReportDTO;
import com.desarrollo.raffy.exception.ResourceNotFoundException;
import com.desarrollo.raffy.model.StatusReport;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reportes", description = "Gestión de reportes de eventos: creación, filtros, revisión y consultas")
public class ReportController {
    
    @Autowired
    private ReportService service;

    @PostMapping
    @Operation(
        summary = "Crear reporte", 
        description = "Crea un reporte sobre un evento")
    public ResponseEntity<?> createReport(
        @Valid @RequestBody CreateReportDTO dto){
        
        if(dto == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Los datos estan vacios");
        }
        log.info("DTO recibido: " + dto);
        log.info("EventId: " + dto.getEventId());
        log.info("Mail: " + dto.getMailUserReport());
        log.info("Reason: " + dto.getReason());
        
        try {
            CreateReportResponseDTO response = service.createReport(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @GetMapping("/event")
    @Operation(summary = "Reportes por evento", 
    description = "Obtiene reportes asociados a un evento")
    public ResponseEntity<?> getReports() {
        try {
            List<AdminEventReportDTO> reports = service.getAllEventsWithReportsSummary();
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>("No se encontraron reportes.", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>("Error al obtener los reportes.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Detalles de reportes por evento", 
    description = "Obtiene todos los reportes de un evento específico con detalles completos")
    public ResponseEntity<?> getEventReportDetails(@PathVariable("eventId") Long eventId) {
        try {
            AdminEventReportDTO eventDetails = service.getEventReportDetails(eventId);
            return new ResponseEntity<>(eventDetails, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error al obtener detalles del evento: {}", e.getMessage());
            return new ResponseEntity<>("Error al obtener detalles del evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // EN REVISIÓN: Endpoint para revisar un reporte (aceptar o rechazar)
    @PutMapping("{reportId}/review")
    @Operation(summary = "Revisar reporte", 
    description = "Actualiza el estado de revisión de un reporte")
    public ResponseEntity<?> reviewReport(
            @PathVariable Long reportId,
            @RequestParam StatusReport status) {

        try {
            ReviewReportDTO updatedReport = service.reviewReport(reportId, status);
            return ResponseEntity.ok(updatedReport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el reporte.");
        }
    }
    /**
     * Endpoint para notificar al creador del evento sobre los 
     * reportes recibidos, con un mensaje personalizado del admin.
     * @param eventId
     * @param adminMessage
     * @return
     */
    @PostMapping("/event/{eventId}/notify-creator")
    @Operation(summary = "Notificar al creador",
        description = "Notifica al creador del evento sobre los reportes recibidos"
    )
    public ResponseEntity<?> notifyCreatorAboutReports(
        @PathVariable Long eventId,
        @RequestParam String adminMessage){
        try {
            service.notifyCreatorAboutReports(eventId, adminMessage);
            return ResponseEntity.ok(new MessagerResponseDTO(
                "Notificación enviada al creador del evento sobre los reportes.")
            );
        } catch (IllegalArgumentException e) {
            log.error("Error al notificar al creador: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.error("Evento no encontrado para notificación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error al enviar notificación al creador: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        }
    }

    @PutMapping("/event/{eventId}/final-decision")
    @Operation(summary = "Decisión final del Administrador",
        description = "El Administrador toma una decisión del en base a los reportes para determinar si sigue o no ACTIVO el evento."
    )
    public ResponseEntity<?> makeFinalDecisionOnEvent(
        @PathVariable Long eventId,
        @RequestParam StatusReport finalStatusReport,
        @RequestParam String adminMessage
    ){
        try {
            if (finalStatusReport == null || adminMessage == null || adminMessage.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El estado y mensaje son requeridos.");
            }
            OrganizerEventReportsDTO dto = service.makeFinalDecisionOnEvent(eventId, finalStatusReport, adminMessage);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al tomar decisión final: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la decisión final.");
        }
    }

    @GetMapping("/has-reported")
    @Operation(summary = "Usuario reportó evento", 
    description = "Verifica si un usuario ha reportado un evento")
    public ResponseEntity<Boolean> hasUserReportedEvent(
            @RequestParam Long eventId,
            @RequestParam String userMail) {
        boolean reported = service.hasUserReportEvent(eventId, userMail);
        return ResponseEntity.ok(reported);
    }
}
