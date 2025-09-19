// package com.desarrollo.raffy.presenter;

// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;


// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import jakarta.validation.Valid;

// import com.desarrollo.raffy.business.services.EventsService;
// import com.desarrollo.raffy.business.services.ParticipantService;
// import com.desarrollo.raffy.business.services.UserService;
// import com.desarrollo.raffy.model.Events;
// import com.desarrollo.raffy.model.GuestUser;
// import com.desarrollo.raffy.model.User;

// @RestController
// @RequestMapping("/events")

// public class ParticipantPresenter {
    
//     @Autowired
//     private UserService userService;

//     @Autowired
//     private ParticipantService participantService;

//     @Autowired
//     private EventsService eventService; 


//     @PostMapping("/{eventId}/participants")
//     public ResponseEntity<Object> registerParticipantToGiveaway(
//         @Valid @RequestBody GuestUser aGuestUser,
//         @PathVariable("eventId") Long aEventId) {
//             if (aGuestUser.getId() != 0) {
//                 return ResponseEntity
//                     .status(HttpStatus.BAD_REQUEST)
//                     .body("Esta intentando crear un guest user. Este no puede tener un id definido.");
//             }

//             Events eventToParticipate = eventService.getById(aEventId);
//             if (eventToParticipate == null) {
//                 return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body("El evento con id " + aEventId + " no existe");
//             }

//             User savedGuestUser;
//             User userFromDb = userService.findByEmail(aGuestUser.getEmail());
            
//             // chequea si ya existe el usuario en la base de datos
//             if (userFromDb == null) {
//                 // si el usuario no existe, lo guarda
//                 savedGuestUser = userService.save(aGuestUser);
//             }
//             else {
//                 // si el usuario existe lo actualizo
//                 userFromDb.setName(aGuestUser.getName());
//                 userFromDb.setSurname(aGuestUser.getSurname());
//                 userFromDb.setCellphone(aGuestUser.getCellphone());
                
//                 savedGuestUser = userService.save(userFromDb);
//             }
//             try {
//                 // intento guardar la participacion del usuario
                
//                 return ResponseEntity
//                     .status(HttpStatus.OK)
//                     .body(participantService.registerToGiveaway(savedGuestUser, eventToParticipate));    
//             } catch (Exception e) {
//                 return ResponseEntity
//                     .status(HttpStatus.CONFLICT)
//                     .body(e.getMessage());
//             }
            
//     }

// }


// // Eliminar si anda todo bien