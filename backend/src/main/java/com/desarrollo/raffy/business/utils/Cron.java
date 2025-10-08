package com.desarrollo.raffy.business.utils;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled; 
import org.springframework.beans.factory.annotation.Autowired;
import com.desarrollo.raffy.business.repository.EventsRepository;
import java.time.LocalDate;
import java.util.List;
import com.desarrollo.raffy.model.Events;

import lombok.extern.slf4j.Slf4j;

import com.desarrollo.raffy.business.services.EventsService;

@Slf4j
@Service
public class Cron {
    public static final String CRON_EXPRESSION_EVERY_DAY = "0 0 0 * * *";

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private EventsService eventsService;

    /*
     * En esta funcion se actualiza el estado de los eventos
     * Si la fecha de finalizacion es igual a la fecha de hoy, se actualiza el estado a CLOSED
     * Si la fecha de finalizacion es anterior a la fecha de hoy, se actualiza el estado a CLOSED
     */
    @Scheduled(cron = CRON_EXPRESSION_EVERY_DAY)
    public void runEvents() {
        List<Events> events = eventsRepository.findOpenEventsToClose(LocalDate.now());
        log.info("Eventos OPEN con fecha vencida o igual a hoy: {}", events.size());
        
        for (Events event : events) {
            log.info("Cerrando evento: {} - {}", event.getId(), event.getTitle());
            try {
                eventsService.closeEvent(event.getId());
            } catch (Exception e) {
                log.error("Error al cerrar evento {}", event.getId(), e);
            }
        }
    }

    
}
