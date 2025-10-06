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
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.User;

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

        if (event.getStatusEvent() != StatusEvent.CLOSED) {
            throw new IllegalStateException("El evento debe estar cerrado para poder ejecutarse");
        }

        List<Participant> participants = participantRepository.findParticipantsByEventId(eventId);

        if(participants.isEmpty()){
            throw new RuntimeException("No hay participantes en este sorteo");
        }

        // Obtener la estrategia adecuada
        WinnerSelectionStrategy strategy = strategyFactory.getStrategy(event.getEventType());
        if (strategy == null) {
            throw new UnsupportedOperationException("No hay estrategia definida para este tipo de evento: " + event.getEventType());
        }

        strategy.selectWinners(event, participants);

        // Persistir cambios (posiciones actualizadas)
        participantRepository.saveAll(participants);

        //Devolver solo los ganadores
        return participants.stream()
        .filter(p -> p.getPosition() > 0)
        .toList();
    }

    public List<Participant> findParticipantsByEventId(Long aEventId) {
        return participantRepository.findParticipantsByEventId(aEventId);
    }

    @Transactional
    public Participant registerToGiveaway(User aUser, Events aGiveaway) {
        if (!(aGiveaway instanceof Giveaways)) {
            throw new IllegalArgumentException("Este metodo solo para registrar usuarios a sorteos");
        }
        if (participantRepository.existsByParticipantAndEvent(aUser, aGiveaway)) {
            throw new IllegalArgumentException("Ya estas inscripto a este sorteo");
        }
        Participant participantToSave = new Participant(aUser, (Giveaways) aGiveaway);
        return participantRepository.save(participantToSave);
    }
    
}
