package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.RaffleNumber;

@Repository

public interface RaffleNumberRepository extends JpaRepository<RaffleNumber, Long> {

    @Query("SELECT rn FROM RaffleNumber rn WHERE rn.event.id = :aEventId")
    public List<RaffleNumber> findNumbersById(@Param("aEventId") Long aEventId);

    @Query("SELECT rn FROM RaffleNumber rn WHERE rn.event.id = :aEventId")
    public List<RaffleNumber> findParticipantsByEventId(@Param("aEventId") Long aEventId);
}
