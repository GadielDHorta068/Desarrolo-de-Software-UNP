package com.desarrollo.raffy.business.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.GuessAttempt;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;


@Component
public class GuessingContestWinnerStrategy implements WinnerSelectionStrategy<Participant> {

    @Autowired
    private AuditLogsService auditLogsService;

    @Override
    public boolean supports(EventTypes eventType) {
        return eventType == EventTypes.GUESSING_CONTEST;
    }

    @Override
    public void selectWinners(Events event, List<Participant> participants) {
        GuessingContest contest = (GuessingContest) event;

        // Reiniciar posiciones
        participants.forEach(p -> p.setPosition((short) 0));

        // Agrupar intentos por usuario
        Map<Long, List<GuessAttempt>> attemptsByUser = contest.getAttempts().stream()
            .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        // Calcular desempeño de cada usuario
        List<UserPerformance> performances = new ArrayList<>();

        for (Map.Entry<Long, List<GuessAttempt>> entry : attemptsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<GuessAttempt> attempts = entry.getValue()
                .stream()
                .sorted(Comparator.comparing(GuessAttempt::getAttemptTime))
                .toList();

            // Buscar primer acierto
            Optional<GuessAttempt> firstCorrect = attempts.stream()
                .filter(a -> a.getGuessedNumber() == contest.getTargetNumber())
                .findFirst();

            if (firstCorrect.isEmpty()) continue; // No adivinó

            int attemptsCount = attempts.indexOf(firstCorrect.get()) + 1;
            Duration totalTime = Duration.between(
                attempts.get(0).getAttemptTime(),
                firstCorrect.get().getAttemptTime()
            );

            performances.add(new UserPerformance(userId, attemptsCount, totalTime));
        }

        // Ordenar por menor cantidad de intentos, luego menor tiempo
        performances.sort(Comparator
            .comparingInt(UserPerformance::attempts)
            .thenComparing(UserPerformance::time));

        // Seleccionar ganadores según winnersCount
        int winnersCount = Math.min(contest.getWinnersCount(), performances.size());
        List<UserPerformance> winners = performances.subList(0, winnersCount);

        // Asignar posición a los participantes ganadores
        AtomicInteger position = new AtomicInteger(1);
        for (UserPerformance wp : winners) {
            participants.stream()
                .filter(p -> p.getParticipant().getId().equals(wp.userId()))
                .findFirst()
                .ifPresent(p -> p.setPosition((short) position.getAndIncrement()));
        }

        AuditEvent auditEvent = auditLogsService.getAuditEventById(contest.getId());
        List<AuditParticipant> auditParticipants = participants.stream()
        .map(p -> new AuditParticipant(
            null,
            p.getPosition(),
            p.getParticipant().getName(),
            p.getParticipant().getSurname(),
            p.getParticipant().getEmail(),
            p.getParticipant().getCellphone(),
            auditEvent
        ))
        .toList();

        auditLogsService.logActionFinalized(
            contest.getId(), 
            contest.getCreator().getNickname(), 
            AuditActionType.EVENT_EXECUTED, 
            String.format("Se Ejecuto la selección de ganadores para el evento: '%s'.", contest.getTitle()), 
            null, 
            auditParticipants);
    }
    /**
     * Clase auxiliar para medir rendimiento de cada usuario.
     */
    private record UserPerformance(Long userId, int attempts, Duration time) {}
}

