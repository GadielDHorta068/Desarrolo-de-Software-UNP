package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrollo.raffy.model.User;

@Repository

public interface ParticipantRepository extends JpaRepository<User, Long> {

}