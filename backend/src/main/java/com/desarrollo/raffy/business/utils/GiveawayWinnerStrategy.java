package com.desarrollo.raffy.business.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.desarrollo.raffy.model.AuditLog;
import com.desarrollo.raffy.model.AuditParticipant;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;

import com.desarrollo.raffy.business.services.AuditLogsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GiveawayWinnerStrategy implements WinnerSelectionStrategy {

    @Autowired
    private AuditLogsService auditLogsService;

    @Override
    public boolean supports(EventTypes eventTypes) {
        return eventTypes == EventTypes.GIVEAWAYS;
    }

    @Override
    public void selectWinners(Events event, List<Participant> participants) {
        Giveaways giveaway = (Giveaways) event;
        AuditLog auditLog = new AuditLog();
        log.info("Número de participantes antes de seleccionar ganadores: " + participants.size());
        log.info("Detalles del sorteo: " + giveaway);
        
        int winnersCount = giveaway.getWinnersCount();
        // Resetea la posición previa
        participants.forEach(p -> p.setPosition((short)0));

        //Barajar
        List<Participant> shuffled = new ArrayList<>(participants);
        
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        Collections.shuffle(shuffled, random);

        //Asignar posiciones
        short pos = 1;
        for(Participant p : shuffled.stream().limit(winnersCount).toList()){
            p.setPosition(pos);
            pos++;
        }

        auditLog.setExecuteDate(LocalDateTime.now());
        auditLog.setCreatorNickname(giveaway.getCreator().getNickname());
        auditLog.setSeed(seed);
        auditLog.setEventId(giveaway.getId());
        auditLog.setEventTitle(giveaway.getTitle());
        auditLog.setEventType(giveaway.getEventType());
        auditLog.setEventStartDate(giveaway.getStartDate());
        auditLog.setEventEndDate(giveaway.getEndDate());

        List<AuditParticipant> auditParticipants = participants.stream()
            .map(p -> new AuditParticipant(
             null,
             p.getParticipant().getName(),
             p.getParticipant().getSurname(),
             p.getParticipant().getEmail(),
             p.getParticipant().getCellphone(),
             p.getPosition()
            ))
            .toList();

        auditLog.setParticipants(auditParticipants);

        auditLogsService.save(auditLog);  
        
        log.info("Auditoría guardada. Seed utilizada: {}", seed);
    }
    
}
