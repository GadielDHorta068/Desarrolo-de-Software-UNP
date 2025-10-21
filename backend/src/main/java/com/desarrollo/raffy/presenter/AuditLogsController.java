package com.desarrollo.raffy.presenter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.auditlog.AuditAction;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;

@RestController
@RequestMapping("/audit")
public class AuditLogsController {
    
    @Autowired
    private AuditLogsService service;

    @PostMapping("/save")
    public ResponseEntity<?> saveAuditLog(@RequestBody AuditEvent auditEvent){
        
        if(auditEvent.getEventId() == null) {
            return new ResponseEntity<>("No hay un usuario relacionado con la auditoria.", HttpStatus.BAD_REQUEST);
        }

        if(auditEvent.getStartDate() == null && auditEvent.getEndDate() == null){
            return new ResponseEntity<>("No hay fecha.", HttpStatus.BAD_REQUEST);
        }

        AuditEvent audit = service.save(auditEvent);
        if(audit != null){   
            return new ResponseEntity<>(audit, HttpStatus.CREATED);
        } else{
            return new ResponseEntity<>("Error al crear la Auditoria.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/filter/event/{creator}/{eventType}/{from}/{to}")
    public ResponseEntity<?> getAuditsByCreator(
        @PathVariable("creator") String creatorEvent,
        @RequestParam(required = false) EventTypes eventTypes,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to
    ){
        try {
            List<AuditEvent> auditEvents = service.getAuditEventByCreator(creatorEvent, eventTypes, from, to);
            if(auditEvents.isEmpty()){
                return new ResponseEntity<>("No hay auditoría para este usuario: " + creatorEvent + ". Revise si tienes sorteos creados", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(auditEvents, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error interno al obtener las auditorías: " + e.getMessage());
        }
    }

    @GetMapping("/filter/action/{eventId}")
    public ResponseEntity<?> getActionsByFilters(
        @PathVariable("eventId") Long eventId,
        @RequestParam(required = false) AuditActionType action,
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        try {
            List<AuditAction> actions = service.getActionsByFilters(eventId, action, from, to);

            if (actions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron acciones para los filtros especificados.");
            }

            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno al obtener las acciones de auditoría: " + e.getMessage());
        }
    }
}
