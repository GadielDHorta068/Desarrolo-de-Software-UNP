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
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.business.services.EventsService;
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

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Events events) {
        // Validar que no exista un evento con el mismo título
        if (eventsService.existsByTitle(events.getTitle())) {
            return new ResponseEntity<>("Ya existe un evento con este título", HttpStatus.CONFLICT);
        }
        
        Events createdEvent = eventsService.create(events);
        if (createdEvent != null) {
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al crear el evento", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
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
    }

    @DeleteMapping("/{id}")
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

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Events> events = eventsService.getAll();
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos", HttpStatus.NOT_FOUND);
        }
    }
    
    // Nuevos endpoints para mapear las consultas del repositorio
    
    @GetMapping("/status/{statusEvent}")
    public ResponseEntity<?> getByStatusEvent(@PathVariable StatusEvent statusEvent) {
        try {
            List<Events> events = eventsService.getByStatusEvent(statusEvent);
            if (events != null && !events.isEmpty()) {
                return new ResponseEntity<>(events, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No se encontraron eventos con el estado: " + statusEvent, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Estado de evento inválido: " + statusEvent, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/type/{eventType}")
    public ResponseEntity<?> getByEventType(@PathVariable EventTypes eventType) {
        try {
            List<Events> events = eventsService.getByEventType(eventType);
            if (events != null && !events.isEmpty()) {
                return new ResponseEntity<>(events, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No se encontraron eventos del tipo: " + eventType, HttpStatus.NOT_FOUND);
            }
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
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos para la categoría ID: " + categoryId, HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEvents() {
        List<Events> events = eventsService.getActiveEvents();
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos activos", HttpStatus.NOT_FOUND);
        }
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
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos en el rango de fechas especificado", HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/start-date/{startDate}")
    public ResponseEntity<?> getByStartDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        if (startDate == null) {
            return new ResponseEntity<>("La fecha de inicio es obligatoria", HttpStatus.BAD_REQUEST);
        }
        
        List<Events> events = eventsService.getByStartDate(startDate);
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos que inicien en la fecha: " + startDate, HttpStatus.NOT_FOUND);
        }
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
        if (events != null && !events.isEmpty()) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se encontraron eventos que contengan: " + title, HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/exists/{title}")
    public ResponseEntity<?> checkTitleExists(@PathVariable String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ResponseEntity<>("El título es obligatorio", HttpStatus.BAD_REQUEST);
        }
        
        boolean exists = eventsService.existsByTitle(title.trim());
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
