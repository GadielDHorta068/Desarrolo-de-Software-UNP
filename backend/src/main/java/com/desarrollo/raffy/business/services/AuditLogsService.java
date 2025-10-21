package com.desarrollo.raffy.business.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.business.repository.AuditActionRepository;
import com.desarrollo.raffy.business.repository.AuditEventRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.auditlog.AuditAction;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;

import jakarta.transaction.Transactional;

@Service
public class AuditLogsService {
    
    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditActionRepository actionRepository;

    @Transactional
    public AuditEvent save(AuditEvent auditevent){
        return auditEventRepository.save(auditevent);
    }

    @Transactional
    public List<AuditEvent> getAuditEventByCreator(String creator, EventTypes type, LocalDate start, LocalDate end){
        List<AuditEvent> auditEvents = auditEventRepository.getEventsByCreator(creator,type,start,end);
        return auditEvents;
    }

    @Transactional
    public List<AuditAction> getActionsByFilters(Long eventId, AuditActionType action, LocalDateTime from, LocalDateTime to){
        List<AuditAction> actions = actionRepository.getByFiltersAction(eventId, action, from, to);
        return actions;
    }

    @Transactional
    public AuditEvent getAuditEventById(Long eventId){
        AuditEvent aEvent = auditEventRepository
        .getAuditEventById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("No se encontro la auditoría"));
        return aEvent;
    }
}
