package com.desarrollo.raffy.business.services;

import java.time.LocalDate;
//Devolver los errores correspondientes
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;

@Service
public class EventsService {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Transactional
    public <T extends Events> T create(T event, Long idUser) {
        // Validar que no exista un evento con el mismo título
        if(eventsRepository.existsByTitle(event.getTitle())){
            throw new IllegalArgumentException("Ya existe un sorteo con el título: "+ event.getTitle());
        }
        Optional<RegisteredUser> creator = registeredUserRepository.findById(idUser);        
        event.setCreator(creator.get());
        event.setStatusEvent(StatusEvent.OPEN);
        event.setStartDate(LocalDate.now());
        return eventsRepository.save(event);
    }

    /**
     * Actualiza un evento existente si el usuario es el creador del evento.
     * @param <T>
     * @param idEvent
     * @param event
     * @param idUser
     * @return El evento actualizado.
     * @throws IllegalArgumentException si el evento no existe o el usuario no es el creador.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public <T extends Events> T update(Long idEvent, T event, Long idUser) {

        Events existing = eventsRepository.findById(idEvent)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        RegisteredUser creator = registeredUserRepository.findById(idUser)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!existing.getCreator().equals(creator)) {
               throw new IllegalArgumentException("No tienes permiso para actualizar este evento");
        }
            
        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setCategory(event.getCategory());
        existing.setEndDate(event.getEndDate());
        existing.setWinnersCount(event.getWinnersCount());

        if(existing instanceof Giveaways && event instanceof Giveaways){
            // No hay campos específicos para actualizar en Giveaways por ahora
        }

        return (T) eventsRepository.save(existing);
    }

    ///Método para traer todos los eventos(Giveaways, Raffles, etc) dado el id del creador
    public List<Events> findByEventsCreator(Long IdCreator){
        return eventsRepository.findByCreatorId(IdCreator);
    }

    public Events getById(Long id) {
        try {
            return eventsRepository.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(Long id) {
        try {
            eventsRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Events> getAll() {
        try {
            List<Events> events = eventsRepository.findAll();
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }       
    }

    // Nuevos métodos para mapear las consultas del repositorio
    
    public List<Events> getByStatusEvent(StatusEvent statusEvent) {
        try {
            List<Events> events = eventsRepository.findByStatusEvent(statusEvent);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByEventType(EventTypes eventType) {
        try {
            List<Events> events = eventsRepository.findByEventType(eventType);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByCategoryId(Long categoryId) {
        try {
            List<Events> events = eventsRepository.findByCategoryId(categoryId);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getActiveEvents() {
        try {
            List<Events> events = eventsRepository.findActiveEvents();
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<Events> events = eventsRepository.findByDateRange(startDate, endDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByStartDate(LocalDate startDate) {
        try {
            List<Events> events = eventsRepository.findByStartDate(startDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByEndDate(LocalDate endDate) {
        try {
            List<Events> events = eventsRepository.findByEndDate(endDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public boolean existsByTitle(String title) {
        try {
            return eventsRepository.existsByTitle(title);
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<Events> searchByTitle(String title) {
        try {
            List<Events> events = eventsRepository.findByTitleContainingIgnoreCase(title);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getEventsByParticipantId(Long userId) {
        try {
            List<Events> events = eventsRepository.findByParticipantId(userId);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

}
