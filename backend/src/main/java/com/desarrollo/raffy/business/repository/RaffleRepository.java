package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.Raffle;

@Repository

public interface RaffleRepository extends JpaRepository<Raffle, Long> {
    
}
