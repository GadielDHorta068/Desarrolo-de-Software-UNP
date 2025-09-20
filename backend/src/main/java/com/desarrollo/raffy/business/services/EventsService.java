package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
//Devolver los errores correspondientes

@Service
public class EventsService {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

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

    public <T extends Events>T update(Long id, T event, RegisteredUser creator) {
        try {
            return eventsRepository.save(event);
        } catch (Exception e) {
            return null;
        }
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
