package com.desarrollo.raffy.presenter;

import org.modelmapper.ModelMapper;
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
import com.desarrollo.raffy.dto.RegisteredUserDTO;
import com.desarrollo.raffy.model.RegisteredUsers;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registered-users")
public class RegisteredUsersController {
    
    @Autowired
    private RegisteredUsersService registeredUsersService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<RegisteredUserDTO> create(@Valid @RequestBody RegisteredUserDTO dto){
        RegisteredUsers entity = modelMapper.map(dto, RegisteredUsers.class);
        RegisteredUsers created = registeredUsersService.create(entity);
        RegisteredUserDTO response = modelMapper.map(created, RegisteredUserDTO.class);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegisteredUserDTO> getById(@PathVariable Long id){
        RegisteredUsers registeredUser = registeredUsersService.getById(id);
        if (registeredUser != null) {
            RegisteredUserDTO response = modelMapper.map(registeredUser, RegisteredUserDTO.class);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegisteredUserDTO> update(@PathVariable Long id, @Valid @RequestBody RegisteredUserDTO dto) {
        dto.setId(id);
        RegisteredUsers entity = modelMapper.map(dto, RegisteredUsers.class);
        RegisteredUsers updated = registeredUsersService.update(entity);
        RegisteredUserDTO response = modelMapper.map(updated, RegisteredUserDTO.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = registeredUsersService.delete(id);
        return new ResponseEntity<>(deleted, deleted ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
