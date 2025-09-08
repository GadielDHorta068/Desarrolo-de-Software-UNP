package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.UserType;

@Service
public class RegisteredUsersService {
    
    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Transactional
    public RegisteredUser create(RegisteredUser registeredUsers){
        try{
            // valor por defecto si no viene como Administrador
            if (registeredUsers.getUserType() == null) {
                registeredUsers.setUserType(UserType.NORMAL);
            }

            if (registeredUserRepository.existsByEmail(registeredUsers.getEmail()) || 
                registeredUserRepository.existsByNickname(registeredUsers.getNickname())) {
                throw new IllegalArgumentException("El usuario con el mismo email o nickname ya existe.");
            }
            return registeredUserRepository.save(registeredUsers);
        } catch(Exception e){
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public RegisteredUser getById(Long id){
        try {
            return registeredUserRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el usuario: " + e.getMessage());
        }
    }

    @Transactional
    public RegisteredUser update(RegisteredUser registeredUsers){
        try {
            Long id = registeredUsers.getId();
            if (id == null) {
                throw new IllegalArgumentException("El id del usuario es requerido para actualizar.");
            }
            // Verifica existencia de email/nickname en otros registros
            if (registeredUserRepository.existsByEmailAndIdNot(registeredUsers.getEmail(), id) || 
                registeredUserRepository.existsByNicknameAndIdNot(registeredUsers.getNickname(), id)) {
                throw new IllegalArgumentException("El usuario con el mismo email o nickname ya existe.");
            }
            return registeredUserRepository.save(registeredUsers);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @Transactional
    public boolean delete(Long id){
        try {
            registeredUserRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al borrar el usuario: " + e.getMessage());
        }
    }
}
