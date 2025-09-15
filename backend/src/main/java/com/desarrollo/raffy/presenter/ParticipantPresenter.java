package com.desarrollo.raffy.presenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.ParticipantService;
import com.desarrollo.raffy.business.services.UserService;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuestUser;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.User;

@RestController
@RequestMapping("participant")

public class ParticipantPresenter {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private EventsService eventService; 


    @PostMapping()
    public ResponseEntity<Object> create(GuestUser aGuestUser, Long aEventId) {
        if (aGuestUser.getId() != 0) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Esta intentando crear un guest user. Este no puede tener un id definido.");
        }
        
        User savedGuestUser = userService.save(aGuestUser);
        Events eventToParticipate = eventService.getById(aEventId);
        if (eventToParticipate != null) {
            Participant participantToSave = new Participant(savedGuestUser, (Giveaways)eventToParticipate);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(participantService.save(participantToSave));
        }
        return null; // Cambiar
    }
}
