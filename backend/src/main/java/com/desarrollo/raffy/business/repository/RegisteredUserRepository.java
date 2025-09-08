package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.RegisteredUser;

@Repository
public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long>{
    
    //Busca si el mail existe
    public boolean existsByEmail(String email);
    
    //Busca si nickname existe
    public boolean existsByNickname( String nickname);

    // Métodos para update: verifica existencia excluyendo un id dado
    public boolean existsByEmailAndIdNot(String email, Long id);
    public boolean existsByNicknameAndIdNot(String nickname, Long id);
}
