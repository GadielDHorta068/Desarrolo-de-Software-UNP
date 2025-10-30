package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.business.utils.WinnerSelectionStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.exception.AlreadyRegisteredToGiveawayExeption;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.auditlog.AuditActionType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RaffleNumberRepository raffleNumberRepository;

    @Autowired
    private WinnerStrategyFactory strategyFactory;

    @Autowired
    private AuditLogsService auditLogsService;

    @Transactional
    public List<?> runEvents(Events event) {
        try {
            log.info("Evento encontrado: {} con estado {}", event.getTitle(), event.getStatusEvent());

            // Cargar datos según el tipo de evento
            if (event instanceof Raffle raffle) {
                List<RaffleNumber> numbers = raffleNumberRepository.findNumbersById(raffle.getId());
                if (numbers.isEmpty()) {
                    throw new RuntimeException("La rifa no tiene números asignados.");
                }

                WinnerSelectionStrategy<RaffleNumber> strategy =
                        strategyFactory.getStrategy(event.getEventType());
                log.info("Estrategia seleccionada: {}", strategy.getClass().getSimpleName());

                strategy.selectWinners(event, numbers);
                raffleNumberRepository.saveAll(numbers);

                return numbers.stream()
                        .filter(n -> n.getPosition() > 0)
                        .toList();

            } else {
                List<Participant> participants = participantRepository.findParticipantsByEventId(event.getId());
                if (participants.isEmpty()) {
                    throw new RuntimeException("No hay participantes en este evento.");
                }

                WinnerSelectionStrategy<Participant> strategy =
                        strategyFactory.getStrategy(event.getEventType());
                log.info("Estrategia seleccionada: {}", strategy.getClass().getSimpleName());

                strategy.selectWinners(event, participants);
                participantRepository.saveAll(participants);

                return participants.stream()
                        .filter(p -> p.getPosition() > 0)
                        .toList();
            }

        } catch (Exception e) {
            log.error("Error al ejecutar el evento: {}", e.getMessage(), e);
            throw e;
        }
    }



    public List<Participant> findParticipantsByEventId(Long aEventId) {
        return participantRepository.findParticipantsByEventId(aEventId);
    }

    @Transactional
    public Participant registerToGiveaway(User aUser, Events aGiveaway) {
        if (!(aGiveaway instanceof Giveaways)) {
            throw new IllegalArgumentException("Este metodo es solo para registrar usuarios a sorteos");
        }
        if (participantRepository.existsByParticipantAndEvent(aUser, aGiveaway)) {
            
            auditLogsService.logAction(
                aGiveaway.getId(), 
                aUser.getName() + " " + aUser.getSurname(), 
                AuditActionType.USER_REGISTERED_FAILED, 
                String.format("Ya está inscrito al evento.")
            );

            throw new AlreadyRegisteredToGiveawayExeption("Ya estás inscripto a este sorteo");
        }
        Participant participantToSave = new Participant(aUser, (Giveaways) aGiveaway);
        
        auditLogsService.logAction(
                aGiveaway.getId(), 
                aUser.getName() + " " + aUser.getSurname(), 
                AuditActionType.USER_REGISTERED, 
                String.format("Se inscribió al evento.")
        );
        return participantRepository.save(participantToSave);
    }
    
}
