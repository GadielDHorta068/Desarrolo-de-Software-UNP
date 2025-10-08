package com.desarrollo.raffy.business.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GiveawayWinnerStrategy implements WinnerSelectionStrategy {

    @Override
    public boolean supports(EventTypes eventTypes) {
        return eventTypes == EventTypes.GIVEAWAYS;
    }

    @Override
    public void selectWinners(Events event, List<Participant> participants) {
        Giveaways giveaway = (Giveaways) event;
        log.info("Número de participantes antes de seleccionar ganadores: " + participants.size());
        log.info("Detalles del sorteo: " + giveaway);
        int winnersCount = giveaway.getWinnersCount();
        // Resetea la posición previa
        participants.forEach(p -> p.setPosition((short)0));

        //Barajar
        List<Participant> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled);

        //Asignar posiciones
        short pos = 1;
        for(Participant p : shuffled.stream().limit(winnersCount).toList()){
            p.setPosition(pos);
            pos++;
        }
    }
    
}
