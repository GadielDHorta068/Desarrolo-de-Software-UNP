package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.GuessProgressRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.business.utils.WinnerSelectionStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.exception.AlreadyRegisteredToGiveawayExeption;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuessProgress;
import com.desarrollo.raffy.model.GuessingContest;
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
    private GuessProgressRepository guessProgressRepository;

    @Autowired
    private WinnerStrategyFactory strategyFactory;

    @Autowired
    private AuditLogsService auditLogsService;

    /*
     * El método runEvents maneja la lógica para ejecutar eventos de diferentes tipos
     * (Raffle, Giveaways, GuessingContest) y seleccionar ganadores utilizando estrategias
     * específicas para cada tipo de evento. El método carga los datos necesarios según el
     * tipo de evento, selecciona la estrategia adecuada, ejecuta la selección de ganadores
     * y guarda los resultados. Finalmente, devuelve una lista de los ganadores.
     */
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
                        strategyFactory.getStrategy(raffle.getEventType());
                log.info("Estrategia seleccionada: {}", strategy.getClass().getSimpleName());

                strategy.selectWinners(event, numbers);
                raffleNumberRepository.saveAll(numbers);

                return numbers.stream()
                        .filter(n -> n.getPosition() > 0)
                        .toList();

            } else if(event instanceof Giveaways giveaways){
                List<Participant> participants = participantRepository.findParticipantsByEventId(giveaways.getId());
                if (participants.isEmpty()) {
                    throw new RuntimeException("No hay participantes en este evento.");
                }

                WinnerSelectionStrategy<Participant> strategy = strategyFactory.getStrategy(giveaways.getEventType());
                log.info("Estrategia seleccionada: {}", strategy.getClass().getSimpleName());

                strategy.selectWinners(event, participants);
                participantRepository.saveAll(participants);

                return participants.stream()
                        .filter(p -> p.getPosition() > 0)
                        .toList();
            } else if(event instanceof GuessingContest guessingContest) {
                List<GuessProgress> attempts = guessProgressRepository.findByContestId(guessingContest.getId());
                if(attempts.isEmpty()){
                    throw new RuntimeException("No hay participantes en este evento.");
                }

                WinnerSelectionStrategy<GuessProgress> strategy = strategyFactory.getStrategy(guessingContest.getEventType());

                strategy.selectWinners(event, attempts);
                guessProgressRepository.saveAll(attempts);

                return attempts.stream()
                            .filter(p -> p.getPosition() > 0)
                            .toList();
            } else {
                throw new IllegalArgumentException("Tipo de evento no soportado para la selección de ganadores.");
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
