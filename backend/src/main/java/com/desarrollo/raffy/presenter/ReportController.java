package com.desarrollo.raffy.presenter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.desarrollo.raffy.model.Report;
import com.desarrollo.raffy.model.StatusReport;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/reports")
@Tag(name = "Reportes", description = "Gestión de reportes de eventos: creación, filtros, revisión y consultas")
public class ReportController {
    
    @Autowired
    private ReportService service;

    @GetMapping
    @Operation(summary = "Listar reportes", description = "Obtiene todos los reportes sin filtros")
    public ResponseEntity<?> getAllReports() {
        try {
            List<Report> reports = service.getAllReportFilter(null, null, null);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener los reportes: {}", e.getMessage());
            return new ResponseEntity<>("Error al obtener los reportes.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Crear reporte", description = "Crea un reporte sobre un evento")
    public ResponseEntity<?> createReport(
        @RequestBody Report report){
        
        Report r = service.createReport(report);

        if(r != null){
            return new ResponseEntity<>(r, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al reportar el evento.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrar reportes", description = "Filtra reportes por estado y rango de fechas")
    public ResponseEntity<?> getReportsFiltered(
            @RequestParam(required = false) StatusReport status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end) {

        List<Report> reports = service.getAllReportFilter(status, start, end);
        if (!reports.isEmpty()) {
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron reportes.", HttpStatus.BAD_REQUEST);
        }   
    }

    @GetMapping("/review/event/{eventId}")
    @Operation(summary = "Reportes por evento", description = "Obtiene reportes asociados a un evento")
    public ResponseEntity<?> getReportsEventId(@PathVariable("eventId") Long eventId) {
        try {
            List<Report> reports = service.getReportEventId(eventId);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>("No se encontraron reportes.", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>("Error al obtener los reportes.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/review")
    @Operation(summary = "Revisar reporte", description = "Actualiza el estado de revisión de un reporte")
    public ResponseEntity<?> reviewReport(
            @RequestParam Long reportId,
            @RequestParam Long eventId,
            @RequestParam StatusReport status) {

        try {
            Report updatedReport = service.reviewReport(reportId, eventId, status);
            return ResponseEntity.ok(updatedReport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el reporte.");
        }
    }

    @GetMapping("/has-reported")
    @Operation(summary = "Usuario reportó evento", description = "Verifica si un usuario ha reportado un evento")
    public ResponseEntity<Boolean> hasUserReportedEvent(
            @RequestParam Long eventId,
            @RequestParam String userMail) {
        boolean reported = service.hasUserReportEvent(eventId, userMail);
        return ResponseEntity.ok(reported);
    }
}
