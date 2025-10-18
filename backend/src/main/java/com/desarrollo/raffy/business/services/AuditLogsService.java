package com.desarrollo.raffy.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.business.repository.AuditLogsRepository;
import com.desarrollo.raffy.model.auditlog.AuditLog;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;

import jakarta.transaction.Transactional;

@Service
public class AuditLogsService {
    
    @Autowired
    private AuditLogsRepository auditLogsRepository;

    @Transactional
    public AuditLog save(AuditLog auditLog){
        return auditLogsRepository.save(auditLog);
    }

    @Transactional
    public List<AuditLog> getAuditLogByCreator(String nickname){
        List<AuditLog> auditLogs = auditLogsRepository.getAuditLogByCreator(nickname)
        .orElseThrow(() -> new IllegalArgumentException("Auditoria no encontrada."));
        return auditLogs;
    }

    @Transactional
    public List<AuditLog> getAuditLogByCreator(
        String nickname, 
        String title, 
        EventTypes type, 
        LocalDateTime from, 
        LocalDateTime to){
        
            
            List<AuditLog> auditLogs = auditLogsRepository.getAuditLogByCreator(nickname,title,type,from,to)
            .orElseThrow(() -> new IllegalArgumentException("Auditoria no encontrada."));
            
            return auditLogs;
    }

    @Transactional
    public List<AuditParticipant> getAuditLogWinnersByEventId(Long eventId){
        List<AuditParticipant> auditParticipants = auditLogsRepository.getAuditLogWinnersByEventId(eventId);
        return auditParticipants;
    }
}
