package com.desarrollo.raffy.business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.GuessProgress;


@Repository
public interface GuessProgressRepository extends JpaRepository<GuessProgress, Long>{
    
    // Ver si ya existe el progreso del usuario en un evento
    @Query("""
        SELECT gp FROM GuessProgress gp
        WHERE gp.contest.id = :contestId
        AND gp.user.id = :userId        
    """)
    Optional<GuessProgress> findByContestIdAndUserId(
        @Param("contestId") Long contestId, 
        @Param("userId") Long userId);
    
    // Ver todos los participantes del evento
    @Query("""
        SELECT gp FROM GuessProgress gp
        WHERE gp.contest.id = :contestId
        ORDER BY gp.attemptTime ASC
    """)
    List<GuessProgress> findByContestId(Long contestId);

    // Ver solo los ganadores del evento
    @Query("""
        SELECT gp FROM GuessProgress gp
        WHERE gp.contest.id = :eventId
        AND gp.hasWon = TRUE
        ORDER BY gp.attemptCount, gp.durationSeconds, gp.attemptTime ASC
    """)
    List<GuessProgress> getWinnersOrdered(@Param("eventId") Long eventId);

    // Ver si el usuario ya ganó
    boolean existsByContestIdAndUserIdAndHasWonTrue(Long contestId, Long userId);

    // Ver si el usuario ya participó en el concurso por su email
    boolean existsByContestIdAndUserEmail(Long contestId, String email);
}