package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.utils.WinnerSelectionStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private WinnerStrategyFactory strategyFactory;

    public List<Participant> runEvent(Events event){
        try {       
            log.info("Evento encontrado: " + event.getTitle() + " con estado " + event.getStatusEvent());

            List<Participant> participants = participantRepository.findParticipantsByEventId(event.getId());
            log.info("Participantes: " + participants);
            if(participants.isEmpty()){
                throw new RuntimeException("No hay participantes en este sorteo");
            }

            // Obtener la estrategia adecuada
            WinnerSelectionStrategy strategy = strategyFactory.getStrategy(event.getEventType());
            log.info("Estrategia seleccionada: " + (strategy != null ? strategy.getClass().getSimpleName() : "Ninguna"));
            if (strategy == null) {
                throw new UnsupportedOperationException("No hay estrategia definida para este tipo de evento: " + event.getEventType());
            }

            strategy.selectWinners(event, participants);
            log.info("Participantes después de seleccionar ganadores: " + participants);
            // Persistir cambios (posiciones actualizadas)
            participantRepository.saveAll(participants);

            //Devolver solo los ganadores
            return participants.stream()
            .filter(p -> p.getPosition() > 0)
            .toList();
        } catch (Exception e) {
            log.error("Error al ejecutar el evento: " + e.getMessage(), e);
            throw e; // Re-lanzar la excepción para que pueda ser manejada por el controlador
        }
        
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
