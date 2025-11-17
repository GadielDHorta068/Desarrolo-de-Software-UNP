package com.desarrollo.raffy.business.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.EventTypes;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long> {
    
    // Buscar eventos por estado
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.statusEvent = :statusEvent AND e.isPrivate = FALSE")
    List<Events> findByStatusEvent(@Param("statusEvent") StatusEvent statusEvent);
    
    // Buscar eventos por tipo
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.eventType = :eventType AND e.isPrivate = FALSE")
    List<Events> findByEventType(@Param("eventType") EventTypes eventType);
    
    // Buscar eventos por categoría
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.category.id = :categoryId AND e.isPrivate = FALSE")
    List<Events> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Buscar eventos con filtros progresivos y estado opcional
    // que no sean creados por el usuario cuyo email se pasa como parámetro
    @Query("""
        SELECT e FROM Events e
        JOIN FETCH e.category c
        JOIN FETCH e.creator cr
        WHERE e.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN
        AND e.eventType = COALESCE(:type, e.eventType)
        AND c.id = COALESCE(:catId, c.id)
        AND e.startDate >= COALESCE(:start, e.startDate)
        AND e.endDate <= COALESCE(:end, e.endDate)
        AND e.winnersCount = COALESCE(:winnerCount, e.winnersCount)
        AND (:email IS NULL OR cr.email <> :email)
        AND e.isPrivate = FALSE      
        ORDER BY e.startDate DESC
    """)
    List<Events> findActiveEvents(
        @Param("type") EventTypes type,
        @Param("catId") Long categoryId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end,
        @Param("winnerCount") Integer winnerCount,
        @Param("email") String email);
    
    // Buscar eventos por rango de fechas
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.startDate >= :startDate AND e.endDate <= :endDate AND e.isPrivate = FALSE")
    List<Events> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Buscar eventos que empiezan en una fecha específica
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.startDate = :startDate AND e.isPrivate = FALSE")
    List<Events> findByStartDate(@Param("startDate") LocalDate startDate);
    
    // Buscar eventos que terminan en una fecha específica
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.endDate = :endDate AND e.isPrivate = FALSE")
    List<Events> findByEndDate(@Param("endDate") LocalDate endDate);
    
    // Verificar si existe un evento con el mismo título
    boolean existsByTitle(String title);
    
    // Buscar eventos por título (búsqueda parcial)
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) AND e.isPrivate = FALSE")
    List<Events> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    // Buscar eventos por participante (usando entidad Participant)
    @Query("SELECT e FROM Participant p JOIN p.event e JOIN FETCH e.creator JOIN FETCH e.category WHERE p.participant.id = :userId")
    List<Events> findByParticipantId(@Param("userId") Long userId);

    // Buscar por fecha que pase a traves del dia de hoy
    @Query("SELECT e FROM Events e WHERE e.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN AND e.endDate <= :today")
    List<Events> findOpenEventsToClose(@Param("today") LocalDate today);

    // buscar participantes de un evento
    @Query("SELECT p.participant FROM Participant p WHERE p.event.id = :eventId")
    List<User> findParticipantsByEventId(@Param("eventId") Long eventId);


    // Buscar eventos por creador
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.creator.id = :creatorId")
    List<Events> findByCreatorId(@Param("creatorId") Long creatorId);

    // Buscar evento por id con detalles necesarios
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.id = :id")
    Optional<Events> findByIdWithDetails(@Param("id") Long id);

    // Buscar todos los eventos con detalles necesarios
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.isPrivate = FALSE")
    List<Events> findAllWithDetails();

    // --------- Métodos para el destacada de cada tipo ---------
    @Query("""
        SELECT g, COUNT(p)
        FROM Giveaways g
        LEFT JOIN Participant p ON p.event = g
        WHERE g.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN AND g.isPrivate = FALSE
        GROUP BY g
        ORDER BY COUNT(p) DESC        
    """)
    List<Object[]> findTopGiveawaysByParticipants(Pageable pageable);

    @Query("""
        SELECT gc, COUNT(a)
        FROM GuessingContest gc
        LEFT JOIN GuessAttempt a ON a.contest = gc
        WHERE gc.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN AND gc.isPrivate = FALSE
        GROUP BY gc
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> findTopGuessingByParticipants(Pageable pageable);

    @Query("""
        SELECT r, COUNT(n)
        FROM Raffle r
        LEFT JOIN RaffleNumber n ON n.raffle = r
        WHERE r.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN AND r.isPrivate = FALSE
        GROUP BY r
        ORDER BY COUNT(n) DESC
        """)
    List<Object[]> findTopRafflesByParticipants(Pageable pageable);

    @Query("""
        SELECT e
        FROM Events e
        WHERE e.statusEvent = com.desarrollo.raffy.model.StatusEvent.OPEN AND e.isPrivate = FALSE
          AND e.eventType = :eventType
        ORDER BY e.startDate DESC
        """)
    List<Events> findRecentByType(@Param("eventType") EventTypes eventType, Pageable pageable);

        // --------- Fin de Métodos para el destacada de cada tipo ---------

}