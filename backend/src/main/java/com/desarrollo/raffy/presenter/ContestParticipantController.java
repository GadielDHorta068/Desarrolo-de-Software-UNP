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
import com.desarrollo.raffy.model.GuessStatus;
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

    @PostMapping("/{contestId}/participants")
    public ResponseEntity<?> registerParticipant(
        @PathVariable("contestId") Long contestId,
        @Valid @RequestBody ParticipantRequestDTO dto,
        @RequestParam(required = false) String invite
        ) {

        GuessingContest contest = (GuessingContest) eventsService.getById(contestId);
        // Verificamos que el evento exista
        if(contest == null){
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("El evento con id "+ contestId + "No existe");
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
        GuessCheckResponseDTO guessCheckResponseDTO = guessProgressService.hasAlreadyParticipated(contestId, dto.getUser().getEmail());
        if (guessCheckResponseDTO.isAlreadyParticipated()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(guessCheckResponseDTO);
        }
        boolean isCreator = false;
        // Si no esta inscripto entonces se crea el usuario
        GuessProgress guessProgress = guessProgressService.register(dto.getGuessProgress(), dto.getUser(), contestId); 

        GuessProgressResponseDTO response = modelMapper.map(guessProgress, GuessProgressResponseDTO.class);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/check-participant/guess/{contestId}")
    public ResponseEntity<?> checkParticipation(
            @PathVariable("contestId") Long contestId,
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
    @GetMapping("/guess/{contestId}/check-number")
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

}
