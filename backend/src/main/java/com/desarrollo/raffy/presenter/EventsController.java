package com.desarrollo.raffy.presenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.business.services.EventsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/events")
public class EventsController {
    @Autowired
    private EventsService eventsService;

    @PostMapping
    public ResponseEntity<Events> create(@RequestBody Events events) {
        Events createdEvent = eventsService.create(events);
        if (createdEvent != null) {
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Events> getById(@PathVariable Long id) {
        Events event = eventsService.getById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Events> update(@PathVariable Long id, @RequestBody Events events) {
        events.setId(id);
        Events updatedEvent = eventsService.update(events);
        if (updatedEvent != null) {
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = eventsService.delete(id);
        if (deleted) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Events>> getAll() {
        List<Events> events = eventsService.getAll();
        if (events != null) {
            return new ResponseEntity<>(events, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
