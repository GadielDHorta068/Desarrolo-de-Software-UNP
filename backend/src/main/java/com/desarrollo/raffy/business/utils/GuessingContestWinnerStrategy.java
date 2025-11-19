package com.desarrollo.raffy.business.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.desarrollo.raffy.business.repository.GuessProgressRepository;
import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.GuessProgress;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.model.auditlog.AuditEvent;
import com.desarrollo.raffy.model.auditlog.AuditParticipant;


@Slf4j
@Component
public class GuessingContestWinnerStrategy implements WinnerSelectionStrategy<GuessProgress> {

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private GuessProgressRepository guessProgressRepository;

    @Override
    public boolean supports(EventTypes eventType) {
        return eventType == EventTypes.GUESSING_CONTEST;
    }

    @Override
    public void selectWinners(Events event, List<GuessProgress> guessProgresses) {
        GuessingContest contest = (GuessingContest) event;
        int targetNumber = contest.getTargetNumber();

        if (guessProgresses == null || guessProgresses.isEmpty()) {
            log.warn("No hay participaciones registradas para este evento.");
            return;
        }

        // Filtrar solo los participantes que acertaron
        List<GuessProgress> winners = guessProgresses.stream()
            .filter(gp -> containsTargetNumber(gp.getNumbersTried(), targetNumber))
            .toList();

        if (winners.isEmpty()) {
            log.info("No hay ganadores para el evento: {}", contest.getTitle());
            logAuditEvent(contest, guessProgresses);
            return;
        }

        // Calcular desempeño de cada ganador
        List<UserPerformance> performances = winners.stream()
            .map(gp -> new UserPerformance(
                gp.getUser().getId(),
                countAttemptsUntilCorrect(gp.getNumbersTried(), targetNumber),
                Duration.ofSeconds(gp.getDurationSeconds()),
                gp
            ))
            .sorted(Comparator
                .comparingInt(UserPerformance::attempts)
                .thenComparing(UserPerformance::time)
                .thenComparing(p -> p.guessProgress().getAttemptTime()))
            .toList();

        // Seleccionar top N ganadores según winnersCount
        int winnersCount = Math.min(contest.getWinnersCount(), performances.size());

        // Asignar posición y marcar como ganadores
        for (int i = 0; i < winnersCount; i++) {
            GuessProgress gp = performances.get(i).guessProgress();
            gp.setPosition((short) (i + 1));
            gp.setHasWon(true);
        }

        // Persistir cambios en la base de datos
        guessProgressRepository.saveAll(guessProgresses);

        // Registrar auditoría
        logAuditEvent(contest, guessProgresses);
    }

    /**
     * Verifica si el número objetivo está presente en el String de intentos
     * 
     * @param numbersTried String con números separados por "," (ej: "10,25,45")
     * @param targetNumber Número que se debe adivinar
     * @return true si el número objetivo está en la lista de intentos
     */
    private boolean containsTargetNumber(String numbersTried, int targetNumber) {
        if (numbersTried == null || numbersTried.isEmpty()) {
            log.warn("numbersTried está vacío o es null");
            return false;
        }

        try {
            return Arrays.stream(numbersTried.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .anyMatch(num -> num == targetNumber);
        } catch (NumberFormatException e) {
            log.error("Error parseando numbersTried: {}", numbersTried, e);
            return false;
        }
    }

    /**
     * Cuenta cuántos intentos realizó el usuario hasta acertar
     * 
     * @param numbersTried String con números separados por "," (ej: "10,25,45")
     * @param targetNumber Número que se debe adivinar
     * @return Cantidad de intentos hasta acertar (1-indexed)
     */
    private int countAttemptsUntilCorrect(String numbersTried, int targetNumber) {
        String[] numbers = numbersTried.split(",");

        for (int i = 0; i < numbers.length; i++) {
            try {
                if (Integer.parseInt(numbers[i].trim()) == targetNumber) {
                    return i + 1; // Retornar posición (1-indexed)
                }
            } catch (NumberFormatException e) {
                log.error("Error parseando número en posición {}: {}", i, numbers[i], e);
            }
        }

        // Fallback: si no encontró el número (no debería pasar si containsTargetNumber funcionó)
        log.warn("No se encontró el targetNumber {} en numbersTried: {}", targetNumber, numbersTried);
        return numbers.length;
    }

    /**
     * Registra el evento de auditoría con todos los participantes
     * 
     * @param contest Concurso que finalizó
     * @param guessProgresses Lista completa de participantes (ganadores y no ganadores)
     */
    private void logAuditEvent(GuessingContest contest, List<GuessProgress> guessProgresses) {
        AuditEvent auditEvent = auditLogsService.getAuditEventById(contest.getId());

        List<AuditParticipant> auditParticipants = guessProgresses.stream()
            .map(p -> new AuditParticipant(
                null,
                p.getPosition(),
                p.getUser().getName(),
                p.getUser().getSurname(),
                p.getUser().getEmail(),
                p.getUser().getCellphone(),
                auditEvent
            ))
            .toList();

        auditLogsService.logActionFinalized(
            contest.getId(),
            contest.getCreator().getNickname(),
            AuditActionType.EVENT_EXECUTED,
            String.format("Se ejecutó la selección de ganadores para el evento: '%s'.", contest.getTitle()),
            null,
            auditParticipants
        );
    }

    /**
     * Clase auxiliar para medir el desempeño de cada usuario.
     * 
     * @param userId ID del usuario
     * @param attempts Cantidad de intentos hasta acertar
     * @param time Duración total en el juego
     * @param guessProgress Referencia al objeto GuessProgress para actualizar
     */
    private record UserPerformance(
        Long userId,
        int attempts,
        Duration time,
        GuessProgress guessProgress
    ) {}
}
