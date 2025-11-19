package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.GuessProgressService;
import com.desarrollo.raffy.dto.CheckGuessNumberDTO;
import com.desarrollo.raffy.dto.GuessCheckResponseDTO;
import com.desarrollo.raffy.dto.GuessProgressResponseDTO;
import com.desarrollo.raffy.dto.ParticipantRequestDTO;
import com.desarrollo.raffy.exception.NoInscriptEventExeption;
import com.desarrollo.raffy.model.GuessProgress;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.StatusEvent;

import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;

@Controller
@RequestMapping("/api/contest")
public class ContestParticipantController {
    
    @Autowired
    private GuessProgressService guessProgressService;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/{eventId}/participants")
    public ResponseEntity<?> registerParticipant(
        @PathVariable("eventId") Long eventId,
        @Valid @RequestBody ParticipantRequestDTO dto
        ) {

        GuessingContest contest = (GuessingContest) eventsService.getById(eventId);
        // Verificamos que el evento exista
        if(contest == null){
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("El evento con id "+ eventId + "No existe");
        }
        // Controlamos que el evento este abierto
        if(contest.getStatusEvent() != StatusEvent.OPEN){
            throw new NoInscriptEventExeption("No es posible inscribirse a este evento");
        }
        // Comprobamos los datos esten cargados
        if(dto == null){
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("No se cargaron los resultados del del evento");
        }
        // Verificamos que el usuario no haya participado antes
        GuessCheckResponseDTO guessCheckResponseDTO = guessProgressService.hasAlreadyParticipated(eventId, dto.getUser().getEmail());
        if (guessCheckResponseDTO.isAlreadyParticipated()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(guessCheckResponseDTO);
        }
        // Si no esta inscripto entonces se crea el usuario
        GuessProgress guessProgress = guessProgressService.register(dto.getGuessProgress(), dto.getUser(), eventId); 

        GuessProgressResponseDTO response = modelMapper.map(guessProgress, GuessProgressResponseDTO.class);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/guess/check")
    public ResponseEntity<?> checkParticipation(
            @RequestParam Long contestId,
            @RequestParam String email) {

        GuessCheckResponseDTO response =
                guessProgressService.hasAlreadyParticipated(contestId, email);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    /**
     * Método para verificar si el usuario acierta o no el número ganador.
     */
    @GetMapping("/{eventId}/guess/check-number")
    public ResponseEntity<CheckGuessNumberDTO> checkGuessNumber(
            @PathVariable Long eventId,
            @RequestParam int guessedNumber) {

        GuessingContest contest = (GuessingContest) eventsService.getById(eventId);

        CheckGuessNumberDTO response = new CheckGuessNumberDTO();
        response.setGuessedNumber(guessedNumber);

        if (guessedNumber > contest.getTargetNumber()) {
            response.setMessage("El número ingresado es mayor al número objetivo.");
        } else if (guessedNumber < contest.getTargetNumber()) {
            response.setMessage("El número ingresado es menor al número objetivo.");
        } else {
            response.setMessage("¡Felicidades! Has adivinado el número correcto.");
        }

        return ResponseEntity.ok(response);
    }
}
