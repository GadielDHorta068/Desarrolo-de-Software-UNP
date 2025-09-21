package com.desarrollo.raffy.presenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuestUser;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.ParticipantService;
import com.desarrollo.raffy.business.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/events")
public class EventsController {
    @Autowired
    private EventsService eventsService;

    /* @Autowired
    private GiveawaysService giveawaysService; */

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipantService participantService;


    @PostMapping("/create/giveaway/{idUser}")
    public ResponseEntity<?> createGiveaway(
        @RequestBody Giveaways giveaways, 
        @PathVariable("idUser") Long idUser) {
        
        Giveaways created = eventsService.create(giveaways, idUser);
        if (created != null) {
            return new ResponseEntity<>(created, HttpStatus.CREATED);
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
        
        Events event = eventsService.getById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Evento no encontrado", HttpStatus.NOT_FOUND);
        }
    }
/* 
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @NotNull @Positive Long id, @Valid @RequestBody Events events) {
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
        
        events.setId(id);
        Events updatedEvent = eventsService.update(events);
        if (updatedEvent != null) {
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al actualizar el evento", HttpStatus.BAD_REQUEST);
        }
    } */

    @DeleteMapping("/id/{id}")
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
        List<Events> events = eventsService.getAll();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/status/{statusEvent}")
    public ResponseEntity<?> getByStatusEvent(@PathVariable StatusEvent statusEvent) {
        try {
            List<Events> events = eventsService.getByStatusEvent(statusEvent);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Estado de evento inválido: " + statusEvent, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/type/{eventType}")
    public ResponseEntity<?> getByEventType(@PathVariable EventTypes eventType) {
        try {
            List<Events> events = eventsService.getByEventType(eventType);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Tipo de evento inválido: " + eventType, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategoryId(@PathVariable @NotNull @Positive Long categoryId) {
        if (categoryId <= 0) {
            return new ResponseEntity<>("El ID de categoría debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getByCategoryId(categoryId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEvents() {
        List<Events> events = eventsService.getActiveEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null || endDate == null) {
            return new ResponseEntity<>("Las fechas de inicio y fin son obligatorias", HttpStatus.BAD_REQUEST);
        }
        
        if (endDate.isBefore(startDate)) {
            return new ResponseEntity<>("La fecha de fin debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getByDateRange(startDate, endDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/start-date/{startDate}")
    public ResponseEntity<?> getByStartDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        if (startDate == null) {
            return new ResponseEntity<>("La fecha de inicio es obligatoria", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getByStartDate(startDate);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @GetMapping("/end-date/{endDate}")
    public ResponseEntity<?> getByEndDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (endDate == null) {
            return new ResponseEntity<>("La fecha de fin es obligatoria", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getByEndDate(endDate);
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos que terminen en la fecha: " + endDate, HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ResponseEntity<>("El título de búsqueda es obligatorio", HttpStatus.BAD_REQUEST);
        }
        
        if (title.trim().length() < 2) {
            return new ResponseEntity<>("El título de búsqueda debe tener al menos 2 caracteres", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.searchByTitle(title.trim());
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
        if (userId <= 0) {
            return new ResponseEntity<>("El ID de usuario debe ser un número positivo", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getEventsByParticipantId(userId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

   /*  @PutMapping("/giveaways")
    public ResponseEntity<Giveaways> updateGiveaway(@Valid @RequestBody Giveaways giveaways ){
        Giveaways updatedGiveaway = giveawaysService.update(giveaways);
        return new ResponseEntity<>(updatedGiveaway, HttpStatus.OK);
    }

    @PutMapping("/giveaways/finalize/{id}")
    public ResponseEntity<Giveaways> finalizedGiveaway(@PathVariable Long id) {
        giveawaysService.finalizedGiveaway(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/giveaways/category/{categoryId}")
    public ResponseEntity<List<Giveaways>> getGivawaysCategoryId(@PathVariable Long categoryId){
        List<Giveaways> giveaways = giveawaysService.findByGiveawaysCategoryId(categoryId);
        return new ResponseEntity<>(giveaways, HttpStatus.OK);
    }

    @GetMapping("/giveaways/active")
    public ResponseEntity<List<Giveaways>> getActiveGiveaways(){
        List<Giveaways> giveaways = giveawaysService.findByActiGiveaways();
        return new ResponseEntity<>(giveaways, HttpStatus.OK);
    }

    @GetMapping("/giveaways/status/{statusEvent}")
    public ResponseEntity<List<Giveaways>> getGiveawayForStatus(StatusEvent statusEvent){
        List<Giveaways> giveaways = giveawaysService.findByStatusGiveaways(statusEvent);
        return new ResponseEntity<>(giveaways, HttpStatus.OK);
    }

    @GetMapping("/giveaways/search")
    public ResponseEntity<List<Giveaways>> getGiveawayDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        List<Giveaways> giveaways = giveawaysService.findByDateRangeGiveaways(startDate, endDate);
        return new ResponseEntity<>(giveaways, HttpStatus.OK);
    } */

    @PostMapping("/id/{eventId}/participants")
    public ResponseEntity<Object> registerParticipantToGiveaway(
        @Valid @RequestBody GuestUser aGuestUser,
        @PathVariable("eventId") Long aEventId) {
            if (aGuestUser.getId() != 0) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Esta intentando crear un guest user. Este no puede tener un id definido.");
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
                
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(participantService.registerToGiveaway(savedGuestUser, eventToParticipate));    
            } catch (Exception e) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
            }
            
    }

}
