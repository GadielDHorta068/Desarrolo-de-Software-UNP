package com.desarrollo.raffy.business.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.User;

@Repository

public interface RaffleNumberRepository extends JpaRepository<RaffleNumber, Long> {

    @Query("SELECT rn FROM RaffleNumber rn WHERE rn.raffle.id = :aEventId")
    public List<RaffleNumber> findNumbersById(@Param("aEventId") Long aEventId);

    @Query("SELECT rn.number FROM RaffleNumber rn WHERE rn.raffle.id = :aEventId")
    public List<Integer> findsoldNumbersById(@Param("aEventId") Long aEventId);

    @Query("SELECT DISTINCT rn.numberOwner FROM RaffleNumber rn WHERE rn.raffle.id = :aEventId")
    public List<User> findParticipantsByEventId(@Param("aEventId") Long aEventId);

    @Query("SELECT DISTINCT rn.numberOwner.email FROM RaffleNumber rn WHERE rn.raffle.id = :aEventId AND rn.position != 0")
    public List<String> findWinnerEmailsByEventId(@Param("aEventId") Long aEventId);

    @Query("SELECT CASE WHEN COUNT(rn) > 0 THEN TRUE ELSE FALSE END "
        + "FROM RaffleNumber rn "
        + "WHERE rn.raffle = :aRaffle AND rn.number = :aNumber")
    public boolean existsByRaffleAndNumber(@Param("aRaffle") Raffle aRaffle, @Param("aNumber") int aNumber);
}
