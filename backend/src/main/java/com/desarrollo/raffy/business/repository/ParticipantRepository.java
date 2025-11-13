package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.Events;

@Repository

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId")
    public List<Participant> findParticipantsByEventId(@Param("eventId") Long eventId);

    public boolean existsByParticipantAndEvent(User aUser, Events aGiveaway);

    @Query("SELECT p.participant.email FROM Participant p WHERE p.event.id = :eventId AND p.position != 0")
    public List<String> findWinnerEmailsByEventId(@Param("eventId") Long eventId);    
}