package com.desarrollo.raffy.business.utils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.business.services.RaffleNumberService;
import com.desarrollo.raffy.model.AuditLog;
import com.desarrollo.raffy.model.AuditParticipant;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RaffleWinnerStrategyFactory implements WinnerSelectionStrategy {

    @Autowired
    private RaffleNumberService raffleNumberService;

    @Autowired
    private AuditLogsService auditLogsService;

    @Override
    public boolean supports(EventTypes eventType) {
        return eventType == EventTypes.RAFFLES;
    }

    @Override
    public void selectWinners(Events event, List<Participant> participants) {
        Raffle raffle = (Raffle) event;

        int winnersCount = raffle.getWinnersCount();
        
        //Buscamos los RaffleNumbers
        List<RaffleNumber> numbers = raffleNumberService.findRaffleNumbersById(raffle.getId());
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

        // Registro las rifas
        AuditLog auditLog = new AuditLog();

        auditLog.setExecuteDate(LocalDateTime.now());
        auditLog.setCreatorNickname(raffle.getCreator().getNickname());
        auditLog.setSeed(seed);
        auditLog.setEventId(raffle.getId());
        auditLog.setEventTitle(raffle.getTitle());
        auditLog.setEventType(raffle.getEventType());
        auditLog.setEventStartDate(raffle.getStartDate());
        auditLog.setEventEndDate(raffle.getEndDate());

        List<AuditParticipant> auditParticipants = numbers.stream()
        .map(n -> new AuditParticipant(
            null,
            n.getNumberOwner().getName(),
            n.getNumberOwner().getSurname(),
            n.getNumberOwner().getEmail(),
            n.getNumberOwner().getCellphone(),
            n.getPosition()
        )).toList();

        auditLog.setParticipants(auditParticipants);

        auditLogsService.save(auditLog);
    }
    
}
