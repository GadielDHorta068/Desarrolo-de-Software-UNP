package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.GuessProgressService;
import com.desarrollo.raffy.business.services.UrlService;
import com.desarrollo.raffy.dto.CheckGuessNumberDTO;
import com.desarrollo.raffy.dto.GuessCheckResponseDTO;
import com.desarrollo.raffy.dto.GuessProgressResponseDTO;
import com.desarrollo.raffy.dto.ParticipantRequestDTO;
import com.desarrollo.raffy.exception.NoInscriptEventExeption;
import com.desarrollo.raffy.model.GuessProgress;
import com.desarrollo.raffy.model.GuessStatus;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

@Controller
@RequestMapping("/api/contest")
@Tag(name = "Participantes de Adivinar el número", description = "Gestión de inscripción: registro, chequeo de participación y verificación de números adivinados")
public class ContestParticipantController {

    @Autowired
    private GuessProgressService guessProgressService;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UrlService urlService;

    @PostMapping("/{contestId}/participants")
    @Operation(summary = "Registrar un participante en un concurso de adivinar el número", description = "Registra un nuevo participante en el concurso especificado, creando un usuario si es necesario.")
    public ResponseEntity<?> registerParticipant(
            @PathVariable("contestId") Long contestId,
            @Valid @RequestBody ParticipantRequestDTO dto,
            @RequestParam(required = false) String invite) {

        GuessingContest contest = (GuessingContest) eventsService.getById(contestId);
        // Verificamos que el evento exista
        if (contest == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("El evento con id " + contestId + "No existe");
        }
        // Controlamos que el evento este abierto
        if (contest.getStatusEvent() != StatusEvent.OPEN) {
            throw new NoInscriptEventExeption("No es posible inscribirse a este evento");
        }
        // Comprobamos los datos esten cargados
        if (dto == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("No se cargaron los resultados del del evento");
        }
        // Verificamos que el usuario no haya participado antes
        GuessCheckResponseDTO guessCheckResponseDTO = guessProgressService.hasAlreadyParticipated(contestId,
                dto.getUser().getEmail());
        if (guessCheckResponseDTO.isAlreadyParticipated()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(guessCheckResponseDTO);
        }

        boolean isCreator = false;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof RegisteredUser u) {
                isCreator = contest.getCreator() != null && contest.getCreator().getId().equals(u.getId());

            }
        } catch (Exception e) {
            isCreator = false;
        }
        if (contest.isPrivate() && !isCreator) {
            // Si el concurso es privado, verificamos el código de invitación
            if (invite == null || invite.isBlank() || urlService.getUrlByShortcodeAndEvent(invite, contestId) == null) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Acceso restringido: Evento privado.");
            }

        }
        // Si no esta inscripto entonces se crea el usuario
        GuessProgress guessProgress = guessProgressService.register(dto.getGuessProgress(), dto.getUser(), contestId);

        GuessProgressResponseDTO response = modelMapper.map(guessProgress, GuessProgressResponseDTO.class);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/check-participant/guess/{contestId}")
    @Operation(summary = "Verificar si un usuario ya participó en un concurso de adivinar el número", description = "Comprueba si un usuario, identificado por su correo electrónico, ya ha participado en el concurso especificado.")
    public ResponseEntity<?> checkParticipation(
            @PathVariable("contestId") Long contestId,
            @RequestParam String email) {

        GuessCheckResponseDTO response = guessProgressService.hasAlreadyParticipated(contestId, email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Método para verificar si el usuario acierta o no el número ganador.
     */
    @GetMapping("/guess/{contestId}/check-number")
    @Operation(summary = "Verificar número adivinado en concurso de adivinar el número", description = "Compara el número adivinado por el participante con el número objetivo del concurso y devuelve si es mayor, menor o si ha ganado.")
    public ResponseEntity<CheckGuessNumberDTO> checkGuessNumber(
            @PathVariable("contestId") Long contestId,
            @RequestParam("guessedNumber") int guessedNumber) {

        GuessingContest contest = (GuessingContest) eventsService.getById(contestId);

        int target = contest.getTargetNumber(); // el número real que hay que adivinar

        CheckGuessNumberDTO response = new CheckGuessNumberDTO();

        if (guessedNumber < target) {
            response.setStatus(GuessStatus.HIGHER);
            response.setMessage("El número objetivo es mayor.");
        } else if (guessedNumber > target) {
            response.setStatus(GuessStatus.LOWER);
            response.setMessage("El número objetivo es menor.");
        } else {
            response.setStatus(GuessStatus.WIN);
            response.setMessage("¡Adivinaste!");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/guess/{contestId}/participants")
    @Operation(summary = "Obtener participantes de un concurso de adivinar el número", description = "Devuelve una lista de participantes inscritos en el concurso especificado.")
    public ResponseEntity<List<GuessProgressResponseDTO>> getGuessProgressByContestId(
            @PathVariable("contestId") Long contestId) {

        List<GuessProgress> gpList = guessProgressService.findGuessProgressesByContestId(contestId);
        List<GuessProgressResponseDTO> responseList = gpList.stream()
                .map(gp -> modelMapper.map(gp, GuessProgressResponseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responseList);
    }

}
