package com.desarrollo.raffy.business.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RaffleWinnerStrategyFactory implements WinnerSelectionStrategy<RaffleNumber> {


    @Autowired
    private AuditLogsService auditLogsService;

    @Override
    public boolean supports(EventTypes eventType) {
        return eventType == EventTypes.RAFFLES;
    }

    @Override
    public void selectWinners(Events event, List<RaffleNumber> participants) {
        Raffle raffle = (Raffle) event;

        int winnersCount = raffle.getWinnersCount();
        
        //Buscamos los RaffleNumbers
        List<RaffleNumber> numbers = participants;
        if(numbers.isEmpty()){
            throw new RuntimeException("La rifa no tiene números asignados.");
        }

        // Resetamos las posiciones
        numbers.forEach(n -> n.setPosition((short)0));

        // Elegir el ganador aleatorio
        long seed = System.currentTimeMillis();
        Collections.shuffle(numbers, new Random(seed));

        short pos = 1;
        for(RaffleNumber rn: numbers.stream().limit(winnersCount).toList()){
            rn.setPosition(pos++);
        }

        AuditEvent auditEvent = auditLogsService.getAuditEventById(raffle.getId());
        List<AuditParticipant> auditParticipants = numbers.stream()
        .map(n -> new AuditParticipant(
            null,
            n.getPosition(),
            n.getNumberOwner().getName(),
            n.getNumberOwner().getSurname(),
            n.getNumberOwner().getEmail(),
            n.getNumberOwner().getCellphone(),
            auditEvent
        ))
        .toList();

        auditLogsService.logActionFinalized(
            raffle.getId(), 
            raffle.getCreator().getNickname(), 
            AuditActionType.EVENT_EXECUTED, 
            String.format("Se Ejecuto la selección de ganadores para el evento: '%s'.", raffle.getTitle()), 
            seed,
            auditParticipants);
    }
    
}
