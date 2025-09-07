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

@Repository
public interface EventsRepository extends JpaRepository<Events, Long> {
    
    // Buscar eventos por estado
    List<Events> findByStatusEvent(StatusEvent statusEvent);
    
    // Buscar eventos por tipo
    List<Events> findByEventType(EventTypes eventType);
    
    // Buscar eventos por categoría
    @Query("SELECT e FROM Events e WHERE e.category.id = :categoryId")
    List<Events> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Buscar eventos activos (no finalizados ni bloqueados)
    @Query("SELECT e FROM Events e WHERE e.statusEvent IN ('OPEN', 'CLOSED')")
    List<Events> findActiveEvents();
    
    // Buscar eventos por rango de fechas
    @Query("SELECT e FROM Events e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
    List<Events> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Buscar eventos que empiezan en una fecha específica
    List<Events> findByStartDate(LocalDate startDate);
    
    // Buscar eventos que terminan en una fecha específica
    List<Events> findByEndDate(LocalDate endDate);
    
    // Verificar si existe un evento con el mismo título
    boolean existsByTitle(String title);
    
    // Buscar eventos por título (búsqueda parcial)
    @Query("SELECT e FROM Events e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Events> findByTitleContainingIgnoreCase(@Param("title") String title);
}