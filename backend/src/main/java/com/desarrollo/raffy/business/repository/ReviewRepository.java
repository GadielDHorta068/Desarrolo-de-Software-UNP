package com.desarrollo.raffy.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Review;
import com.desarrollo.raffy.model.User;

@Repository

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.event.creator.email = :creatorEmail")
    public List<Review> findReviewsByEventCreatorEmail(@Param("creatorEmail") String creatorEmail);

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.event.creator.email = :userEmail")
    public Double getReputationOfUserByUserEmail(@Param("userEmail") String userEmail);

    @Query ("SELECT COUNT(r) > 0 FROM Review r WHERE r.user = :aUser AND r.event = :anEvent")
    public boolean existsByUserAndEvent(@Param("aUser") User aUser, @Param("anEvent") Events anEvent);

}