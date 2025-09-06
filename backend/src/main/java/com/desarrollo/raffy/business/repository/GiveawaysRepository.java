package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.Giveaways;


@Repository
public interface GiveawaysRepository extends JpaRepository<Giveaways, Long>{
    
}
