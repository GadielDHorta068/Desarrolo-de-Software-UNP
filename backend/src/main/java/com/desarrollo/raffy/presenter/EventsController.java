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
import com.desarrollo.raffy.util.ImageUtils;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.ParticipantService;
import com.desarrollo.raffy.business.services.UserService;
import com.desarrollo.raffy.business.services.GiveawaysService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.desarrollo.raffy.dto.EventSummaryDTO;
import org.modelmapper.ModelMapper;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/events")
public class EventsController {
    @Autowired
    private EventsService eventsService;

    @Autowired
    private GiveawaysService giveawaysService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ModelMapper modelMapper;


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
            EventSummaryDTO dto = eventsService.getEventSummaryById(created.getId());
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

    @PutMapping("update/{idEvent}/user/{idUser}")
    public ResponseEntity<?> update(
                    @PathVariable("idEvent") @NotNull @Positive Long id, 
                    @RequestBody Events events, 
                    @PathVariable("idUser") Long idUser) {
        if (id <= 0) {
            return new ResponseEntity<>("El ID debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        
        // Verificar que el evento existe
        Events existingEvent = eventsService.getById(id);
        if (existingEvent == null) {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
        
        // Validar que no exista otro evento con el mismo título (excepto el actual)
        if (!existingEvent.getTitle().equals(events.getTitle()) && eventsService.existsByTitle(events.getTitle())) {
            return new ResponseEntity<>("Ya existe otro evento con este título", HttpStatus.CONFLICT);
        }
        
        // Validaciones de fechas (la fecha de inicio es la del evento existente)
        if (events.getEndDate() == null) {
            return new ResponseEntity<>("Debe especificar la fecha de fin del evento", HttpStatus.BAD_REQUEST);
        }
        if (!events.getEndDate().isAfter(existingEvent.getStartDate())) {
            return new ResponseEntity<>("La fecha de fin debe ser posterior a la fecha de inicio del evento", HttpStatus.BAD_REQUEST);
        }
        
        events.setId(id);
        Events updatedEvent = eventsService.update(id, events, idUser);
        if (updatedEvent != null) {
            EventSummaryDTO dto = eventsService.getEventSummaryById(updatedEvent.getId());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al actualizar el evento", HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("/event-types")
    public ResponseEntity<?> getAllEventTypes() {
        EventTypes[] eventTypes = eventsService.getAllEventTypes();
        return new ResponseEntity<>(eventTypes, HttpStatus.OK);
    }

    @DeleteMapping("delete/id/{id}")
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

    @GetMapping("/active")
    public ResponseEntity<?> getActiveEvents() {
        List<EventSummaryDTO> events = eventsService.getActiveEventSummaries();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

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
        List<EventSummaryDTO> events = eventsService.getEventSummariesByParticipantId(userId);
        if (events.isEmpty()) {
            return new ResponseEntity<>("No se encontraron eventos para el participante con ID: " + userId, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PostMapping("/{eventId}/participants")
    public ResponseEntity<Object> registerParticipantToGiveaway(
        @Valid @RequestBody GuestUser aGuestUser,
        @PathVariable("eventId") Long aEventId) {
            if (aGuestUser.getId() != null && aGuestUser.getId() != 0) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Esta intentando crear un guest user. Este no puede tener un id definido.");
            }
            // normalizar id=0 a null para permitir persistencia con IDENTITY
            if (aGuestUser.getId() != null && aGuestUser.getId() == 0) {
                aGuestUser.setId(null);
            }

            Events eventToParticipate = eventsService.getById(aEventId);
            if (eventToParticipate == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El evento con id " + aEventId + " no existe");
            }

            User savedGuestUser;
            User userFromDb = userService.findByEmail(aGuestUser.getEmail());
            
            // chequea si ya existe el usuario en la base de datos
            if (userFromDb == null) {
                // si el usuario no existe, lo guarda
                savedGuestUser = userService.save(aGuestUser);
            }
            else {
                // si el usuario existe lo actualizo
                userFromDb.setName(aGuestUser.getName());
                userFromDb.setSurname(aGuestUser.getSurname());
                userFromDb.setCellphone(aGuestUser.getCellphone());
                
                savedGuestUser = userService.save(userFromDb);
            }
            try {
                // intento guardar la participacion del usuario
                Participant created = participantService.registerToGiveaway(savedGuestUser, eventToParticipate);
                Map<String, Object> response = new HashMap<>();
                response.put("id", created.getId());
                response.put("eventId", eventToParticipate.getId());
                response.put("userId", savedGuestUser.getId());
                response.put("message", "Inscripción realizada exitosamente");
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
            } catch (Exception e) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
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
        var giveaways = giveawaysService.findByDateRangeGiveaways(startDate, endDate);
        var events = giveaways.stream()
            .map(g -> modelMapper.map(g, EventSummaryDTO.class))
            .toList();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

}
