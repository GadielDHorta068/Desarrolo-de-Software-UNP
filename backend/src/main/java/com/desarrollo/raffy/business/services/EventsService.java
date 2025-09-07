package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.business.repository.EventsRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.time.LocalDate;
//Devolver los errores correspondientes
@Service
public class EventsService {

    public Events create(Events events) {
        try {
            return eventsRepository.save(events);
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

    public Events update(Events events) {
        try {
            return eventsRepository.save(events);
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
            return (List<Events>) eventsRepository.findAll();
        } catch (Exception e) {
            return null;
        }       
    }

    // Nuevos métodos para mapear las consultas del repositorio
    
    public List<Events> getByStatusEvent(StatusEvent statusEvent) {
        try {
            return eventsRepository.findByStatusEvent(statusEvent);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getByEventType(EventTypes eventType) {
        try {
            return eventsRepository.findByEventType(eventType);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getByCategoryId(Long categoryId) {
        try {
            return eventsRepository.findByCategoryId(categoryId);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getActiveEvents() {
        try {
            return eventsRepository.findActiveEvents();
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            return eventsRepository.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getByStartDate(LocalDate startDate) {
        try {
            return eventsRepository.findByStartDate(startDate);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Events> getByEndDate(LocalDate endDate) {
        try {
            return eventsRepository.findByEndDate(endDate);
        } catch (Exception e) {
            return null;
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
            return eventsRepository.findByTitleContainingIgnoreCase(title);
        } catch (Exception e) {
            return null;
        }
    }

    @Autowired
    private EventsRepository eventsRepository;
}
