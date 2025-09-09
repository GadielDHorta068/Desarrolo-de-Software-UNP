package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.utils.WinnerSelectionStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Participant;

@Service

public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private WinnerStrategyFactory strategyFactory;

    public List<Participant> runEvent(Long eventId){
        Events event = eventsRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        List<Participant> participants = participantRepository.findByEventId(eventId);

        if(participants.isEmpty()){
            throw new RuntimeException("No hay participantes en este sorteo");
        }

        // Obtener la estrategia adecuada
        WinnerSelectionStrategy strategy = strategyFactory.getStrategy(event.getEventType());

        strategy.selectWinners(event, participants);

        // Persistir cambios (posiciones actualizadas)
        participantRepository.saveAll(participants);

        //Devolver solo los ganadores
        return participants.stream()
        .filter(p -> p.getPosition() > 0)
        .toList();
    }

    @Transactional
    public Participant save(Participant aParticipant){
        return participantRepository.save(aParticipant);
    }
    
}
