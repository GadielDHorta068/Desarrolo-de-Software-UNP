package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.EventTypes;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long> {
    
    // Buscar eventos por estado
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.statusEvent = :statusEvent")
    List<Events> findByStatusEvent(@Param("statusEvent") StatusEvent statusEvent);
    
    // Buscar eventos por tipo
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.eventType = :eventType")
    List<Events> findByEventType(@Param("eventType") EventTypes eventType);
    
    // Buscar eventos por categoría
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.category.id = :categoryId")
    List<Events> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Buscar eventos activos (no finalizados ni bloqueados)
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.statusEvent IN ('OPEN', 'CLOSED')")
    List<Events> findActiveEvents();
    
    // Buscar eventos por rango de fechas
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
    List<Events> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Buscar eventos que empiezan en una fecha específica
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.startDate = :startDate")
    List<Events> findByStartDate(@Param("startDate") LocalDate startDate);
    
    // Buscar eventos que terminan en una fecha específica
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.endDate = :endDate")
    List<Events> findByEndDate(@Param("endDate") LocalDate endDate);
    
    // Verificar si existe un evento con el mismo título
    boolean existsByTitle(String title);
    
    // Buscar eventos por título (búsqueda parcial)
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Events> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    // Buscar eventos por participante
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category JOIN e.participants p WHERE p.id = :userId")
    List<Events> findByParticipantId(@Param("userId") Long userId);

    // Buscar por fecha que pase a traves del dia de hoy
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.startDate <= :today AND e.endDate >= :today")
    List<Events> findByToday(@Param("today") LocalDate today);

    // Buscar eventos por creador
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.creator.id = :creatorId")
    List<Events> findByCreatorId(@Param("creatorId") Long creatorId);

    // Buscar evento por id con detalles necesarios
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category WHERE e.id = :id")
    Optional<Events> findByIdWithDetails(@Param("id") Long id);

    // Buscar todos los eventos con detalles necesarios
    @Query("SELECT e FROM Events e JOIN FETCH e.creator JOIN FETCH e.category")
    List<Events> findAllWithDetails();
}