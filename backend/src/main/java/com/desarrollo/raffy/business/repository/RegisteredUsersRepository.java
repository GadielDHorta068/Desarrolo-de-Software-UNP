package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.RegisteredUsers;

@Repository
public interface RegisteredUsersRepository extends JpaRepository<RegisteredUsers, Long>{
    
    //Busca si el mail existe
    public boolean existsByEmail(String email);
    
    //Busca si nickname existe
    public boolean existsByNickname( String nickname);
}
