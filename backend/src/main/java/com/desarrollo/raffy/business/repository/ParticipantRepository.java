package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

<<<<<<< HEAD
=======
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

>>>>>>> edc7c3b01ceee5336a51c7503034ef9328cd97a2
import com.desarrollo.raffy.model.Participant;

@Repository

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId")
    public List<Participant> findByEventId(@Param("eventId") Long eventId);

    public void saveAll(List<Participant> participants);
}