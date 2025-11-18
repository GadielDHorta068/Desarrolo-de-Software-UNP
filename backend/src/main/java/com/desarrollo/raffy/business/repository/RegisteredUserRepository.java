package com.desarrollo.raffy.business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.RegisteredUser;

@Repository
public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long>{
    
    //Busca si el mail existe
    public boolean existsByEmail(String email);
    
    //Busca si nickname existe
    public boolean existsByNickname( String nickname);
    
    // Métodos para buscar usuarios
    Optional<RegisteredUser> findByEmail(String email);
    
    Optional<RegisteredUser> findByNickname(String nickname);

    // Búsqueda parcial por nickname (case-insensitive)
    List<RegisteredUser> findByNicknameContainingIgnoreCase(String nickname);

    @Query("SELECT r FROM RegisteredUser r WHERE "
            + "LOWER(r.nickname) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.surname) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT(r.name, ' ', r.surname)) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<RegisteredUser> searchByQuery(@Param("q") String q);

    // Métodos para update: verifica existencia excluyendo un id dado
    @Query("SELECT COUNT(r) > 0 FROM RegisteredUser r WHERE r.email = :email AND r.id != :id")
    public boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    @Query("SELECT COUNT(r) > 0 FROM RegisteredUser r WHERE r.nickname = :nickname AND r.id != :id")
    public boolean existsByNicknameAndIdNot(@Param("nickname") String nickname, @Param("id") Long id);
}
