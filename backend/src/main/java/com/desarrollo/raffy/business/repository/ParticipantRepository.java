package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Participant;

@Repository

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId")
    public List<Participant> findByEventId(@Param("eventId") Long eventId);

    public void saveAll(List<Participant> participants);
}