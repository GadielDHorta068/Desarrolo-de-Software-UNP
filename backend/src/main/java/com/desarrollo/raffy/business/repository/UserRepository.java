package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrollo.raffy.model.User;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
   
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}