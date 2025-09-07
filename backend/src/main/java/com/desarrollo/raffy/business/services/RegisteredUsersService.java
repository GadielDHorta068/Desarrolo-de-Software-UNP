package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RegisteredUsersRepository;
import com.desarrollo.raffy.model.RegisteredUsers;
import com.desarrollo.raffy.model.UserTypes;

@Service
public class RegisteredUsersService {
    
    @Autowired
    private RegisteredUsersRepository registeredUsersRepository;

    @Transactional
    public RegisteredUsers create(RegisteredUsers registeredUsers){
        try{
            // valor por defecto si no viene como Administrador
            if (registeredUsers.getUserType() == null) {
                registeredUsers.setUserType(UserTypes.NORMAL);
            }

            if (registeredUsersRepository.existsByEmail(registeredUsers.getEmail()) || 
                registeredUsersRepository.existsByNickname(registeredUsers.getNickname())) {
                throw new IllegalArgumentException("El usuario con el mismo email o nickname ya existe.");
            }
            return registeredUsersRepository.save(registeredUsers);
        } catch(Exception e){
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public RegisteredUsers getById(Long id){
        try {
            return registeredUsersRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el usuario: " + e.getMessage());
        }
    }

    @Transactional
    public RegisteredUsers update(RegisteredUsers registeredUsers){
        try {
            Long id = registeredUsers.getId();
            if (id == null) {
                throw new IllegalArgumentException("El id del usuario es requerido para actualizar.");
            }
            // Verifica existencia de email/nickname en otros registros
            if (registeredUsersRepository.existsByEmailAndIdNot(registeredUsers.getEmail(), id) || 
                registeredUsersRepository.existsByNicknameAndIdNot(registeredUsers.getNickname(), id)) {
                throw new IllegalArgumentException("El usuario con el mismo email o nickname ya existe.");
            }
            return registeredUsersRepository.save(registeredUsers);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @Transactional
    public boolean delete(Long id){
        try {
            registeredUsersRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al borrar el usuario: " + e.getMessage());
        }
    }
}
