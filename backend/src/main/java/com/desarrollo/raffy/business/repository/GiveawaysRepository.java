package com.desarrollo.raffy.business.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.StatusEvent;

@Repository
public interface GiveawaysRepository extends JpaRepository<Giveaways, Long> {

    List<Giveaways> findByStatusEvent(StatusEvent statusEvent);

    List<Giveaways> findByEventType(EventTypes eventType);

    @Query("SELECT g FROM Giveaways g WHERE g.category.id = :categoryId")
    List<Giveaways> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT g FROM Giveaways g WHERE g.startDate >= :startDate AND g.endDate <= :endDate")
    List<Giveaways> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT g FROM Giveaways g WHERE g.statusEvent IN (com.desarrollo.raffy.model.StatusEvent.OPEN, com.desarrollo.raffy.model.StatusEvent.CLOSED)")
    List<Giveaways> findByActiveEvent();

    boolean existsByTitle(String title);

    @Query("SELECT g FROM Giveaways g WHERE LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Giveaways> findByTitleEvent(@Param("title")String title);
}
