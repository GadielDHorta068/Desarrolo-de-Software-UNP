package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.RegisteredUsersService;
import com.desarrollo.raffy.model.RegisteredUsers;

@RestController
@RequestMapping("/api/registered-users")
public class RegisteredUsersController {
    
    @Autowired
    private RegisteredUsersService registeredUsersService;

    @PostMapping
    public ResponseEntity<RegisteredUsers> create(@RequestBody RegisteredUsers registeredUsers){
        RegisteredUsers createRegisteredUsers = registeredUsersService.create(registeredUsers);
        if (createRegisteredUsers != null) {
            return new ResponseEntity<>(createRegisteredUsers, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(createRegisteredUsers, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegisteredUsers> getById(@PathVariable Long id){
        RegisteredUsers registeredUser = registeredUsersService.getById(id);
        if (registeredUser != null) {
            return new ResponseEntity<>(registeredUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(registeredUser, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegisteredUsers> update(@PathVariable Long id, @RequestBody RegisteredUsers registeredUsers) {
        registeredUsers.setId(id);
        RegisteredUsers updatedRegisteredUsers = registeredUsersService.update(registeredUsers);
        if (updatedRegisteredUsers != null) {
            return new ResponseEntity<>(updatedRegisteredUsers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = registeredUsersService.delete(id);
        if (deleted) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }
}
