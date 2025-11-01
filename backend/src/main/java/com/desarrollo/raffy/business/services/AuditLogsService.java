package com.desarrollo.raffy.business.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.AuditActionRepository;
import com.desarrollo.raffy.business.repository.AuditEventRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.auditlog.AuditAction;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;


@Service
public class AuditLogsService {
    
    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditActionRepository actionRepository;

    @Transactional
    public AuditEvent save(AuditEvent auditevent){
        try {
            return auditEventRepository.save(auditevent);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al guardar la auditoría: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventByCreator(String creator, EventTypes type, LocalDate start, LocalDate end){
        List<AuditEvent> auditEvents = auditEventRepository.getEventsByCreator(creator,type,start,end);
        return auditEvents;
    }

    @Transactional(readOnly = true)
    public List<AuditAction> getActionsByFilters(Long eventId, AuditActionType action, LocalDateTime from, LocalDateTime to){
        List<AuditAction> actions = actionRepository.getByFiltersAction(eventId, action, from, to);
        return actions;
    }

    @Transactional(readOnly = true)
    public AuditEvent getAuditEventById(Long relatedEventId){
        return auditEventRepository
        .findByRelatedEventId(relatedEventId)
        .orElseThrow(() -> new IllegalArgumentException("No se encontro la auditoría."));
    }

    /**
     * Método que se encarga de auditar cuando un evento es creado. Se crea la auditoría.
     * @param created
     * @return
     */
    @Transactional
    public AuditEvent createAuditEvent(Events created){
        // Creamos la auditoría
            AuditEvent auditEvent = new AuditEvent();

            auditEvent.setRelatedEventId(created.getId());
            auditEvent.setCreatorEvent(created.getCreator().getNickname());
            auditEvent.setTitle(created.getTitle());
            auditEvent.setType(created.getEventType());
            auditEvent.setStartDate(created.getStartDate());
            auditEvent.setEndDate(created.getEndDate());

            //Creamos la acción
            AuditAction createAction = new AuditAction();
            createAction.setEvent(auditEvent);
            createAction.setActorIdentifier(created.getCreator().getNickname());
            createAction.setAction(AuditActionType.EVENT_CREATED);
            createAction.setTimestamp(LocalDateTime.now());
            createAction.setDetails(String.format(
                "Se creo el sorteo '%s' por el usuario '%s'.", 
                created.getTitle(), 
                created.getCreator().getNickname()
            ));

            // Agregamos la acción en auditEvent
            auditEvent.getActions().add(createAction);

            //Guardamos la auditoría
            return save(auditEvent);
    }

    /**
     * Este método se utilizará cuando el evento se Modifique, cierre o un participante se inscriba
     * @param relatedEventId
     * @param actor
     * @param type
     * @param details
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Long relatedEventId, String actor, AuditActionType type, String details){
        AuditEvent auditEvent = getAuditEventById(relatedEventId);
        
        auditEvent.getActions().add(buildAction(auditEvent, actor, type, details));
        save(auditEvent);
    }

    /**
     * Una vez finalizado el evento, audito la acción, guardo la semilla utilizada para barajear y guardo todos los participantes
     * @param relatedEventId
     * @param actor
     * @param type
     * @param details
     * @param seed
     * @param participants
     */
    @Transactional
    public void logActionFinalized(Long relatedEventId, String actor, AuditActionType type, String details, Long seed, List<AuditParticipant> participants){
        AuditEvent auditEvent = getAuditEventById(relatedEventId);

        //Una vez finalizado, guardamos la semilla
        auditEvent.setSeed(seed);
        
        auditEvent.getActions().add(buildAction(auditEvent, actor, type, details));
        auditEvent.getParticipants().addAll(participants);
        save(auditEvent);

    }

    private AuditAction buildAction(AuditEvent event, String actor, AuditActionType type, String details) {
        AuditAction action = new AuditAction();
        action.setEvent(event);
        action.setActorIdentifier(actor);
        action.setAction(type);
        action.setDetails(details);
        action.setTimestamp(LocalDateTime.now());
        return action;
    }

    /**
     * Filtrar los los ganadores por la posición que quedaron en el sorteo
     * @param eventId
     * @return
     */
    @Transactional(readOnly = true)
    public List<AuditParticipant> getAuditWinnersByEventId(Long eventId){
        AuditEvent auditEvent = getAuditEventById(eventId);
        List<AuditParticipant> winners = auditEvent.getParticipants().stream()
            .filter(participant -> participant.getUserPosition() > 0)
            .sorted(Comparator.comparingInt(AuditParticipant::getUserPosition))
            .toList();
        return winners;
    }
}
