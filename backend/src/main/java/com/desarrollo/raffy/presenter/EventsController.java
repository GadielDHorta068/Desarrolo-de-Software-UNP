package com.desarrollo.raffy.presenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.GuestUser;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.business.services.AuditLogsService;
import com.desarrollo.raffy.business.services.EmailService;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.ParticipantService;
import com.desarrollo.raffy.business.services.RaffleNumberService;
import com.desarrollo.raffy.business.services.UserMapper;
import com.desarrollo.raffy.business.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.desarrollo.raffy.dto.BuyRaffleNumberRequestDTO;
import com.desarrollo.raffy.dto.EventSummaryDTO;
import com.desarrollo.raffy.dto.ParticipantDTO;
import com.desarrollo.raffy.dto.RaffleParticipantDTO;
import com.desarrollo.raffy.dto.UserDTO;
import com.desarrollo.raffy.dto.WinnerDTO;

import com.desarrollo.raffy.exception.NoInscriptEventExeption;


import org.modelmapper.ModelMapper;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/events")
public class EventsController {
    @Autowired
    private EventsService eventsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private RaffleNumberService raffleNumberService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogsService auditLogsService;


    @PostMapping("/create/giveaway/{idUser}")
    public ResponseEntity<?> createGiveaway(
        @RequestBody Giveaways giveaways, 
        @PathVariable("idUser") Long idUser) {
        
        // Validaciones de fechas
        if (giveaways.getEndDate() == null) {
            return new ResponseEntity<>("Debe especificar la fecha de fin del evento", HttpStatus.BAD_REQUEST);
        }
        if (giveaways.getEndDate().isBefore(LocalDate.now())) {
            return new ResponseEntity<>("La fecha de fin debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }
        
        Giveaways created = eventsService.create(giveaways, idUser);

        if (created != null) {
            auditLogsService.createAuditEvent(created);
            EventSummaryDTO dto = eventsService.getEventSummaryById(created.getId());
            
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al crear el evento", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create/guessing-contest/{idUser}")
    public ResponseEntity<?> createGuessingContest(
        @RequestBody GuessingContest guessingContest, 
        @PathVariable("idUser") Long idUser) {
        
        // Validaciones de fechas
        if (guessingContest.getEndDate() == null) {
            return new ResponseEntity<>("Debe especificar la fecha de fin del evento", HttpStatus.BAD_REQUEST);
        }
        if (guessingContest.getEndDate().isBefore(LocalDate.now())) {
            return new ResponseEntity<>("La fecha de fin debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }
        
        GuessingContest created = eventsService.create(guessingContest, idUser);
        if (created != null) {
            auditLogsService.createAuditEvent(created);
            EventSummaryDTO dto = eventsService.getEventSummaryById(created.getId());
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al crear el evento", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create/raffle/{idUser}")
    public ResponseEntity<?> createRaffle(
        @RequestBody Raffle aRaffle,
        @PathVariable("idUser") Long idUser
    ) {
        
        // Validaciones de fechas
        if (aRaffle.getEndDate() == null) {
            return new ResponseEntity<>("Debe especificar la fecha de fin del evento", HttpStatus.BAD_REQUEST);
        }
        if (aRaffle.getEndDate().isBefore(LocalDate.now())) {
            return new ResponseEntity<>("La fecha de fin debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }
        
        Raffle newRaffle = eventsService.create(aRaffle, idUser);
        if (newRaffle != null) {
            auditLogsService.createAuditEvent(newRaffle);
            EventSummaryDTO dto = eventsService.getEventSummaryById(newRaffle.getId());
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al crear el evento", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }


    @GetMapping("/id/{id}")
    public ResponseEntity<?> getById(@PathVariable @NotNull @Positive Long id) {
        if (id <= 0) {
            return new ResponseEntity<>("El ID debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        
        EventSummaryDTO event = eventsService.getEventSummaryById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/status/id/{id}")
    public ResponseEntity<?> getStatusById(@PathVariable @NotNull @Positive Long id) {
        if (id <= 0) {
            return new ResponseEntity<>("El ID debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        // VOLVER
        EventSummaryDTO event = eventsService.getEventSummaryById(id);
        if (event != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", id);
            eventData.put("status", event.getStatusEvent());
            // eventData.put("status", "elEstado");
            // return Response.response("OK", "Recurso encontrado", eventData);
            return Response.responseOk("Recurso encontrado", eventData);
            // return new ResponseEntity<>(eventData, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/giveaway/{idEvent}/user/{idUser}")
    public ResponseEntity<?> updateGiveaway(
            @PathVariable Long idEvent,
            @PathVariable Long idUser,
            @RequestBody Giveaways event) {

        Giveaways updatedEvent = eventsService.update(idEvent, event, idUser);
        //Auditoría
        auditLogsService.logAction(
            idEvent,
            updatedEvent.getCreator().getNickname(),
            AuditActionType.EVENT_UPDATED,
            String.format("El evento: '%s' se actualizó.", updatedEvent.getTitle())
        );
        //Fin auditoría
        eventsService.getEventSummaryById(updatedEvent.getId());
        return ResponseEntity.ok("Evento actualizado correctamente.");
    }

    @PutMapping("/update/guessing-contest/{idEvent}/user/{idUser}")
    public ResponseEntity<?> updateGuessingConstest(
            @PathVariable Long idEvent,
            @PathVariable Long idUser,
            @RequestBody GuessingContest event) {

        GuessingContest updatedEvent = eventsService.update(idEvent, event, idUser);
        //Auditoría
        auditLogsService.logAction(
            idEvent,
            updatedEvent.getCreator().getNickname(),
            AuditActionType.EVENT_UPDATED,
            String.format("El evento: '%s' se actualizó.", updatedEvent.getTitle())
        );
        //Fin auditoría
        eventsService.getEventSummaryById(updatedEvent.getId());
        return ResponseEntity.ok("Evento actualizado correctamente.");
    }

    @PutMapping("/update/raffle/{idEvent}/user/{idUser}")
    public ResponseEntity<?> updateRaffle(
        @PathVariable Long idEvent,
        @PathVariable Long idUser,
        @RequestBody Raffle event
    ){
        Raffle updatedEvent = eventsService.update(idEvent, event, idUser);
        //Auditoría
        auditLogsService.logAction(
            idEvent,
            updatedEvent.getCreator().getNickname(),
            AuditActionType.EVENT_UPDATED,
            String.format("El evento: '%s' se actualizó.", updatedEvent.getTitle())
        );
        //Fin auditoría
        eventsService.getEventSummaryById(updatedEvent.getId());
        return ResponseEntity.ok("Evento actualizado correctamente.");
    }


    @GetMapping("/creator/{idCreator}")
    public ResponseEntity<?> getEventsByCreator(@PathVariable Long idCreator){
        List<Events> events = eventsService.findByEventsCreator(idCreator);
        if(events.isEmpty()){
            return new ResponseEntity<>("No se encontraron eventos para el creador con ID: " + idCreator, HttpStatus.NOT_FOUND);
        }
        List<EventSummaryDTO> response = events.stream()
            .map(eventsService::toEventSummaryDTO).collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retorna los ganadores de un evento.
     */
    @GetMapping("/winners/event/{eventId}")
    public ResponseEntity<?> getWinnersParticipantByEventId(@PathVariable("eventId") Long eventId){
        try {
            // Obtener los ganadores del evento
            List<?> winners = eventsService.finalizedEvent(eventId);
            //log.info("Número de ganadores obtenidos: " + winners.size());
            
            if(winners.isEmpty()){
                return new ResponseEntity<>("No se encontraron ganadores para el evento con ID: " + eventId, HttpStatus.NOT_FOUND);
            }
            
            // Convertir la lista de participantes a WinnerDTO
            List<WinnerDTO> response = winners.stream()
                .map(this::toWinnerDTO)
                .filter(Objects::nonNull)
                .toList();
            //log.info("WinnerDTO generados: " + response.size());
            
            // Obtener la información del evento para enviar en los correos
            //log.info("Obteniendo información del evento con ID: " + eventId);
            EventSummaryDTO event = eventsService.getEventSummaryById(eventId);
            if (event == null) {
                log.warn("No se pudo obtener la información del evento con ID: " + eventId);
                return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
            }
            //log.info("Evento obtenido: " + event.getTitle() + " - Tipo: " + event.getEventType());
            
            // Enviar correos a los ganadores con la plantilla personalizada
            String eventTypeStr = event.getEventType() != null ? event.getEventType().toString() : "GIVEAWAYS";
            //log.info("Iniciando envío de correos a los ganadores...");
            try {
                // Preparar datos de contacto del creador del evento
                String creatorName = null;
                String creatorEmail = null;
                String creatorPhone = null;
                if (event.getCreator() != null) {
                    String name = event.getCreator().getName();
                    String surname = event.getCreator().getSurname();
                    String nickname = event.getCreator().getNickname();
                    String composedName = ((name != null ? name : "") + (surname != null ? (" " + surname) : "")).trim();
                    creatorName = !composedName.isEmpty() ? composedName : (nickname != null ? nickname : null);
                    creatorEmail = event.getCreator().getEmail();
                    creatorPhone = event.getCreator().getCellphone();
                }

                emailService.sendWinnerEmails(
                    response,
                    event.getId(),
                    event.getTitle(),
                    eventTypeStr,
                    creatorName,
                    creatorEmail,
                    creatorPhone
                );
                // Enviar resumen de contacto de ganadores al creador
                try {
                    emailService.sendWinnersContactToCreator(
                        response,
                        event.getId(),
                        event.getTitle(),
                        eventTypeStr,
                        creatorName,
                        creatorEmail
                    );
                } catch (Exception e) {
                    log.warn("No se pudo enviar el resumen de ganadores al creador: {}", e.getMessage());
                }
                log.info("Correos electrónicos enviados a los ganadores del evento: " + event.getTitle());
            } catch (Exception e) {
                log.error("Error al enviar correos a los ganadores: " + e.getMessage(), e);
                // Continuar aunque falle el envío de correos
            } 


            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>("Error al finalizar el evento", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al obtener los ganadores: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @GetMapping("/participants/event/{eventId}")
    public ResponseEntity<?> getParticipantsByEventId(@PathVariable("eventId") Long eventId){
        try {
            List<Participant> participants = participantService.findParticipantsByEventId(eventId);
            log.info("Número de participantes obtenidos: " + participants.size());
            if(participants.isEmpty()){
                return new ResponseEntity<>("No se encontraron participantes para el evento con ID: " + eventId, HttpStatus.NOT_FOUND);
            }
            
            // Crear DTOs para participantes (sin información sensible)
            List<ParticipantDTO> response = participants.stream()
                .map(this::toParticipantDTO)
                .toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener participantes: " + e.getMessage(), e);
            return new ResponseEntity<>("Error al obtener los participantes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retorna un ganador específico (WinnerDTO) por evento y posición.
     * No ejecuta la finalización ni envía correos; solo consulta.
     */
    @GetMapping("/winner/event/{eventId}/position/{position}")
    public ResponseEntity<?> getWinnerByEventAndPosition(
            @PathVariable("eventId") Long eventId,
            @PathVariable("position") short position) {
        if (eventId == null || eventId <= 0) {
            return new ResponseEntity<>("El ID del evento debe ser positivo", HttpStatus.BAD_REQUEST);
        }
        if (position <= 0) {
            return new ResponseEntity<>("La posición debe ser mayor a 0", HttpStatus.BAD_REQUEST);
        }
        Events event = eventsService.getById(eventId);
        if (event == null) {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
        if (event.getStatusEvent() != StatusEvent.FINALIZED) {
            return new ResponseEntity<>("El evento aún no está finalizado", HttpStatus.CONFLICT);
        }

        Object winnerObj = null;
        try {
            if (event instanceof Raffle) {
                List<RaffleNumber> numbers = raffleNumberService.findRaffleNumbersById(eventId);
                if (numbers != null) {
                    for (RaffleNumber rn : numbers) {
                        if (rn.getPosition() == position) {
                            winnerObj = rn;
                            break;
                        }
                    }
                }
            } else {
                List<Participant> participants = participantService.findParticipantsByEventId(eventId);
                if (participants != null) {
                    for (Participant p : participants) {
                        if (p.getPosition() == position) {
                            winnerObj = p;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener ganador: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error interno al obtener el ganador", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (winnerObj == null) {
            return new ResponseEntity<>("No se encontró ganador para la posición indicada", HttpStatus.NOT_FOUND);
        }

        WinnerDTO dto = toWinnerDTO(winnerObj);
        if (dto == null) {
            return new ResponseEntity<>("No se pudo mapear el ganador", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Retorna una lista de ganadores (WinnerDTO) de un evento ya finalizado.
     * No ejecuta la finalización ni envía correos; solo consulta.
     */
    @GetMapping("/winners/event/{eventId}/list")
    public ResponseEntity<?> getWinnersListByEventId(@PathVariable("eventId") Long eventId) {
        if (eventId == null || eventId <= 0) {
            return new ResponseEntity<>("El ID del evento debe ser positivo", HttpStatus.BAD_REQUEST);
        }

        Events event = eventsService.getById(eventId);
        if (event == null) {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }

        if (event.getStatusEvent() != StatusEvent.FINALIZED) {
            return new ResponseEntity<>("El evento aún no está finalizado", HttpStatus.CONFLICT);
        }

        try {
            List<WinnerDTO> response;
            if (event instanceof Raffle) {
                List<RaffleNumber> numbers = raffleNumberService.findRaffleNumbersById(eventId);
                response = numbers.stream()
                    .filter(n -> n.getPosition() > 0)
                    .map(this::toWinnerDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            } else {
                List<Participant> participants = participantService.findParticipantsByEventId(eventId);
                response = participants.stream()
                    .filter(p -> p.getPosition() > 0)
                    .map(this::toWinnerDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }

            if (response == null || response.isEmpty()) {
                return new ResponseEntity<>("No se encontraron ganadores para el evento", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener lista de ganadores: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error interno al obtener los ganadores", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private WinnerDTO toWinnerDTO(Object winner) {
        WinnerDTO dto = new WinnerDTO();
        if(winner instanceof Participant participant){
            dto.setParticipantId(participant.getParticipant().getId());
            dto.setName(participant.getParticipant().getName());
            dto.setSurname(participant.getParticipant().getSurname());
            dto.setPosition(participant.getPosition());
            dto.setEmail(participant.getParticipant().getEmail());
            dto.setPhone(participant.getParticipant().getCellphone());
            dto.setEventId(participant.getEvent().getId());
            dto.setEventTitle(participant.getEvent().getTitle());
        } else if (winner instanceof RaffleNumber raffleNumber) {
            dto.setParticipantId(raffleNumber.getNumberOwner().getId());
            dto.setName(raffleNumber.getNumberOwner().getName());
            dto.setSurname(raffleNumber.getNumberOwner().getSurname());
            dto.setEmail(raffleNumber.getNumberOwner().getEmail());
            dto.setPhone(raffleNumber.getNumberOwner().getCellphone());
            dto.setPosition(raffleNumber.getPosition());
            dto.setEventId(raffleNumber.getRaffle().getId());
            dto.setEventTitle(raffleNumber.getRaffle().getTitle());
            dto.setRaffleNumber(raffleNumber.getNumber()); // Incluir el número de la rifa
        } 
        else {
            log.warn("Tipo de ganador desconocido: {}", winner.getClass().getSimpleName());
            return null;
        }
            
        return dto;
            
    }

    private ParticipantDTO toParticipantDTO(Participant participant) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setParticipantId(participant.getParticipant().getId());
        dto.setName(participant.getParticipant().getName());
        dto.setSurname(participant.getParticipant().getSurname());
        dto.setPosition(participant.getPosition());
        dto.setEventId(participant.getEvent().getId());
        dto.setEventTitle(participant.getEvent().getTitle());
        return dto;
    }

    @GetMapping("/event-types")
    public ResponseEntity<?> getAllEventTypes() {
        EventTypes[] eventTypes = eventsService.getAllEventTypes();
        return new ResponseEntity<>(eventTypes, HttpStatus.OK);
    }

    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<?> delete(@PathVariable @NotNull @Positive Long id) {
        if (id <= 0) {
            return new ResponseEntity<>("El ID debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        
        // Verificar que el evento existe
        Events existingEvent = eventsService.getById(id);
        if (existingEvent == null) {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
        
        boolean deleted = eventsService.delete(id);
        if (deleted) {
            return new ResponseEntity<>("Evento eliminado exitosamente", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al eliminar el evento", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        List<EventSummaryDTO> events = eventsService.getAllEventSummaries();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/status/{statusEvent}")
    public ResponseEntity<?> getByStatusEvent(@PathVariable StatusEvent statusEvent) {
        List<EventSummaryDTO> events = eventsService.getEventSummariesByStatus(statusEvent);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<?> getByEventType(@PathVariable EventTypes eventType) {
        List<EventSummaryDTO> events = eventsService.getEventSummariesByEventType(eventType);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategoryId(@PathVariable @NotNull @Positive Long categoryId) {
        List<EventSummaryDTO> events = eventsService.getEventSummariesByCategoryId(categoryId);
        if (events.isEmpty()) {
            return new ResponseEntity<>("No se encontraron eventos para la categoría con ID: " + categoryId, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    //Mejorar para los filtros
    //-------------------------- GADIEL, ESTOS SON LOS FILTROS --------------------------
    
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEvents(
            @RequestParam(required = false) EventTypes type,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer winnerCount,
            @RequestParam(required = false) String emailUserRegister) {

        
        if (start != null && end != null && !end.isAfter(start)) {
            return ResponseEntity.badRequest().body("La fecha de fin debe ser posterior a la fecha de inicio.");

        }
        
        List<EventSummaryDTO> events = eventsService.getActiveEventSummaries(
                type, categorie, start, end, winnerCount, emailUserRegister
        );

        return ResponseEntity.ok(events);
    }
    //-------------------------- OBVIAMENTE ACA TERMINA LOS FILTROS --------------------------


    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Validación de rango de fechas
        if (endDate.isBefore(startDate)) {
            return new ResponseEntity<>("La fecha de inicio no puede ser posterior a la fecha de fin", HttpStatus.BAD_REQUEST);
        }
        List<EventSummaryDTO> events = eventsService.getEventSummariesByDateRange(startDate, endDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/start-date/{startDate}")
    public ResponseEntity<?> getByStartDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        List<EventSummaryDTO> events = eventsService.getEventSummariesByStartDate(startDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/end-date/{endDate}")
    public ResponseEntity<?> getByEndDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EventSummaryDTO> events = eventsService.getEventSummariesByEndDate(endDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        List<EventSummaryDTO> events = eventsService.searchEventSummariesByTitle(title);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/exists/{title}")
    public ResponseEntity<?> checkTitleExists(@PathVariable String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ResponseEntity<>("El título es obligatorio", HttpStatus.BAD_REQUEST);
        }
        
        boolean exists = eventsService.existsByTitle(title.trim());
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
    
    @GetMapping("/participant/{userId}")
    public ResponseEntity<?> getEventsByParticipantId(@PathVariable @NotNull @Positive Long userId) {
        // Validar existencia del usuario
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<>("Usuario no encontrado con ID: " + userId, HttpStatus.NOT_FOUND);
        }

        // Devolver siempre 200 con lista (vacía o con datos)
        List<EventSummaryDTO> events = eventsService.getEventSummariesByParticipantId(userId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PostMapping("/{eventId}/participants")
    public ResponseEntity<Object> registerParticipantToGiveaway(
        @Valid @RequestBody UserDTO aGuestUser,
        @PathVariable("eventId") Long aEventId) {

            Events eventToParticipate = eventsService.getById(aEventId);
            if (eventToParticipate == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El evento con id " + aEventId + " no existe");
            }
            // controlamos que el evento no haya cerrado
            if(eventToParticipate.getStatusEvent() != StatusEvent.OPEN){
                // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                //     .body("El evento con id " + aEventId + " se encuentra cerrado. No es posible inscribirse. TESTTTT");
                throw new NoInscriptEventExeption("No es posible inscribirse a este evento");
            }

            User savedGuestUser = createOrUpdateUser(aGuestUser);
            
            // intento guardar la participacion del usuario
            @SuppressWarnings("unused")
            Participant created = participantService.registerToGiveaway(savedGuestUser, eventToParticipate);
            
            return Response.ok(null,"Inscripción exitosa");
            // cambiar el null por un dto o manejar la referencia circular q hay entre giveaway y
            //su organizador en caso de querer retornar el objeto Participant 

    }

    @GetMapping("/raffle/{eventId}/sold-numbers")
        public ResponseEntity<Object> getSoldNumbersById(@PathVariable("eventId") Long aRaffleId) {
            try {
                List<Integer> someSoldNumbers = raffleNumberService.findSoldNumbersById(aRaffleId);
                if (someSoldNumbers == null) {
                    someSoldNumbers = Collections.emptyList();
                }
                return ResponseEntity.ok(someSoldNumbers);
            }
            catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
            } 
            catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los números vendidos: " + e.getMessage());
        }
    }

    @PostMapping("/{eventId}/buy-raffle-number")
    public ResponseEntity<Object> buyRaffleNumber(
        @Valid @RequestBody BuyRaffleNumberRequestDTO aBuyRequest,
        @PathVariable("eventId") Long aEventId) {

        // System.out.println("aBuyRequest: " + aBuyRequest);
        // System.out.println("aBuyRequest.getAGuestUser(): " + aBuyRequest.getAGuestUser());
        UserDTO aGuestUser = aBuyRequest.getAGuestUser();
        
        // Buscar el evento (Raffle)
        Raffle eventToParticipate = (Raffle) eventsService.getById(aEventId);
        if (eventToParticipate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("El evento con id " + aEventId + " no existe");
        }
    
        // Buscar usuario existente o crear uno nuevo
        User savedGuestUser = createOrUpdateUser(aGuestUser);
    
        // Crear los números comprados
        @SuppressWarnings("unused")
        List<RaffleNumber> someBoughtRaffleNumbers = 
            raffleNumberService.createRaffleNumbers(
                eventToParticipate,
                savedGuestUser,
                aBuyRequest.getSomeNumbersToBuy()
            );
    
        return Response.ok(null, "Números adquiridos exitosamente");
    }

    private User createOrUpdateUser(UserDTO aUserCandidate) {
        // User savedGuestUser;
        User userFromDb = userService.findByEmail(aUserCandidate.getEmail());
        
        // chequea si ya existe el usuario en la base de datos
        if (userFromDb == null) {
            // si el usuario no existe:
            // Crea un nuevo usuario
            GuestUser guestUserToSave = new GuestUser();
            guestUserToSave.setName(aUserCandidate.getName());
            guestUserToSave.setSurname(aUserCandidate.getSurname());
            guestUserToSave.setEmail(aUserCandidate.getEmail());
            guestUserToSave.setCellphone(aUserCandidate.getCellphone());

            // Guarda el nuevo usuario
            return userService.save(guestUserToSave);
        }
        else {
            // si el usuario existe:
            // lo actualiza
            userFromDb.setName(aUserCandidate.getName());
            userFromDb.setSurname(aUserCandidate.getSurname());
            userFromDb.setCellphone(aUserCandidate.getCellphone());
            
            // Guarda el usuario actualizado
            return userService.save(userFromDb);
        }
    }


    @GetMapping("/giveaways/search")
    public ResponseEntity<?> searchGiveawaysByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            return new ResponseEntity<>("La fecha de inicio no puede ser posterior a la fecha de fin", HttpStatus.BAD_REQUEST);
        }
        var giveaways = eventsService.getByDateRange(startDate, endDate);
        var events = giveaways.stream()
            .map(g -> modelMapper.map(g, EventSummaryDTO.class))
            .toList();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping("/{idEvent}/status/user/{idUser}")
    public ResponseEntity<?> updateStatusEvent(
            @PathVariable("idEvent") Long idEvent,
            @PathVariable("idUser") Long idUser,
            @RequestBody Map<String, String> payload) {

        // Validar existencia del evento
        Events existingEvent = eventsService.getById(idEvent);
        if (existingEvent == null) {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }

        // Validar que el usuario sea el creador del evento
        if (existingEvent.getCreator() == null || !existingEvent.getCreator().getId().equals(idUser)) {
            return new ResponseEntity<>("No autorizado: solo el creador puede cambiar el estado", HttpStatus.FORBIDDEN);
        }

        // Validar cuerpo de la petición
        String statusStr = payload.get("statusEvent");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return new ResponseEntity<>("Debe especificar el estado del evento (statusEvent)", HttpStatus.BAD_REQUEST);
        }

        @SuppressWarnings("unused")
        StatusEvent requestedStatus;
        try {
            requestedStatus = StatusEvent.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("Estado inválido: " + statusStr, HttpStatus.BAD_REQUEST);
        }

        if(existingEvent.getStatusEvent() != StatusEvent.OPEN){
            return new ResponseEntity<>("Solo se pueden cerrar eventos que estén en estado OPEN", HttpStatus.BAD_REQUEST);
        }

        try {
                eventsService.closeEvent(idEvent);
        } catch (Exception e) {
            return new ResponseEntity<>("No se pudo cerrar el evento: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        EventSummaryDTO dto = eventsService.getEventSummaryById(idEvent);
        auditLogsService.logAction(
                idEvent,
                dto.getCreator().getNickname(),
                AuditActionType.EVENT_CLOSED, 
                String.format("El evento '%s' se cerró.", dto.getTitle())
                );
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    // REFACTORIZAR LOS SIG DOS METODOS EN UNO
    // QUE OBTENGAN EL TIPO DE EVENTO DE LA URL Y DECIDA COMO OBTENER LOS PARTICIPANTES
    @GetMapping("/{eventId}/get-users-participants")
    public ResponseEntity<Object> findUsersParticipantsByEventId(
        @PathVariable("eventId") Long anEventId,
        @RequestParam(required = false) String aUserEmail) {
            try {
                List<RaffleParticipantDTO> result = eventsService.getUsersParticipantsByEventId(anEventId, aUserEmail);
                if (result == null || result.isEmpty()) {
                    return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
                }
                                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            catch (Exception e) {
                return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
    }

    @GetMapping("/{eventId}/get-raffle-owners")
    public ResponseEntity<Object> getRaffleOwnersByRaffleId(@PathVariable("eventId") Long aRaffleId) {
        // validar q sea tipo rifa???
        try {
            List<User> raffleOwners = raffleNumberService.findRaffleOwnersByRaffleId(aRaffleId);
            if (raffleOwners == null || raffleOwners.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }

            List<UserDTO> result = raffleOwners.stream().map(UserMapper::toDTO).toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }    
    }

    @GetMapping("/{eventId}/get-raffle-participants")
    public ResponseEntity<Object> getRaffleNumbersByRaffleIdAndUser(
        @PathVariable("eventId") Long aRaffleId, 
        @RequestParam(required = false) String aUserEmail) {
            try {
                List<RaffleParticipantDTO> result = raffleNumberService.findRaffleNumbersById(aRaffleId, aUserEmail);
                if (result == null || result.isEmpty()) {
                    return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
                }
                    return new ResponseEntity<>(result, HttpStatus.OK);
            }
            catch (Exception e) {
                return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
    }
}
