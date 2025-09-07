package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.business.repository.EventsRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
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

    @Autowired
    private EventsRepository eventsRepository;
}
