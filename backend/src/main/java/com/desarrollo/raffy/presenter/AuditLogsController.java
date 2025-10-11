package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.model.AuditLog;
import com.desarrollo.raffy.model.AuditParticipant;

@RestController
@RequestMapping("/Audit")
public class AuditLogsController {
    
    @Autowired
    private AuditLogsService service;

    @PostMapping("/save")
    public ResponseEntity<?> saveAuditLog(@RequestBody AuditLog auditLog){
        
        if(auditLog.getCreatorNickname() == null || auditLog.getCreatorNickname().isBlank()) {
            return new ResponseEntity<>("No hay un usuario relacionado con la auditoria.", HttpStatus.BAD_REQUEST);
        }

        if(auditLog.getExecuteDate() == null){
            return new ResponseEntity<>("No hay fecha y hora de ejecución.", HttpStatus.BAD_REQUEST);
        }

        AuditLog auditLog2 = service.save(auditLog);
        if(auditLog2 != null){   
            return new ResponseEntity<>(auditLog2, HttpStatus.CREATED);
        } else{
            return new ResponseEntity<>("Error al crear la Auditoria.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/obtain/nickname/{userNickname}")
    public ResponseEntity<?> getAuditLogByCreator(@PathVariable("userNickname") String userNickname){
        try {
            List<AuditLog> auditLogs = service.getAuditLogByCreator(userNickname);

            if(auditLogs.isEmpty()){
                return new ResponseEntity<>("No se encontraron Auditoria para el usuario: " + userNickname, HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(auditLogs, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>("Error al encontrar las auditorias", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/obtain/event/{eventId}/winners")
    public ResponseEntity<?> getWinnersAuditByEvent(@PathVariable Long eventId) {
        List<AuditParticipant> winners = service.getAuditLogWinnersByEventId(eventId);

        if (winners.isEmpty()) {
            return new ResponseEntity<>("No se encontraron ganadores para el evento con ID: " + eventId, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(winners, HttpStatus.OK);
    }

}
