package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ReportRepository;
import com.desarrollo.raffy.exception.ResourceNotFoundException;
import com.desarrollo.raffy.model.EventScoreStatus;
import com.desarrollo.raffy.model.Events;

@Service
public class ScoreService {
    
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EventsRepository eventsRepository;


    /**
     * Calcula el score considerando antecedentes graves del creador
     * @param eventId
     * @return
     */
    @Transactional(readOnly = true)
    public int calculateScoreEvent(Long eventId){
        
    if(eventId == null){
            throw new IllegalArgumentException("El ID del evento no puede ser nulo.");
        }
        
        Events event = eventsRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado."));
    
        int reportEvents = reportRepository.countByEventId(eventId);
        int blockedEvents = eventsRepository.countBlockedByCreatorId(event.getCreator().getId());
        int finalizedEvents = eventsRepository.countFinalizedByCreatorId(event.getCreator().getId());

        int totalReportsBlocked = reportRepository.countTotalReportsByCreatorBlockedEvents(eventId);

        int penaltyForBlockedEvents = (totalReportsBlocked > 50) ? 10 : 0;

        int score = (reportEvents * 2) + (blockedEvents * 5) - (finalizedEvents * 2) + penaltyForBlockedEvents;
        
        return Math.max(score, 0);
    }

    /**
     * Determina el estado basado en el score
     * @param score
     * @return
     */
    public EventScoreStatus getStatusByScore(int score){
        if(score < 5){
            return EventScoreStatus.ACTIVE;
        } else if(score >= 5 && score <= 9){
            return EventScoreStatus.WARNING;
        } else if(score >= 10 && score <= 14){
            return EventScoreStatus.REVIEW;
        } else{
            return EventScoreStatus.SUSPENDED;
        }
    }

    @Transactional(readOnly = true)
    public String getRecommendation(Long eventId){
        int score = calculateScoreEvent(eventId);
        EventScoreStatus status = getStatusByScore(score);
        Events event = eventsRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("Evento no encotrado."));

        int totalReportsBlocked = reportRepository.countTotalReportsByCreatorBlockedEvents(event.getCreator().getId());

        return switch(status){
            case ACTIVE -> totalReportsBlocked > 50
                ? "El evento está activo pero el organizador tiene antecedentes graves. Se recomienda monitoreo constante."
                : "El evento está en condiciones normales. No se requiere acción.";
            case WARNING -> "El evento tiene algunos reportes. Se recomienda monitoreo. Score: " + score;
            case REVIEW -> "El evento está bajo revisión debido a múltiples reportes. Se requiere investigación. Score: " + score;
            case SUSPENDED -> " El evento debe ser suspendido inmediatamente por su historial de reportes y antecedentes. Score: " + score;
        };
    }
}
