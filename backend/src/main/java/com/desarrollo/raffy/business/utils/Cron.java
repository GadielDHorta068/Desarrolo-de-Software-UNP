package com.desarrollo.raffy.business.utils;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled; 
import org.springframework.beans.factory.annotation.Autowired;
import com.desarrollo.raffy.business.repository.EventsRepository;
import java.time.LocalDate;
import java.util.List;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.business.services.EventsService;

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
        List<Events> events = eventsRepository.findByToday(LocalDate.now());
        for (Events event : events) {
            if((event.getEndDate().isEqual(LocalDate.now()) || event.getEndDate().isBefore(LocalDate.now())) 
                && event.getStatusEvent() != StatusEvent.CLOSED){

                this.eventsService.closeEvent(event.getId());
            }
        }
    }
    
}
