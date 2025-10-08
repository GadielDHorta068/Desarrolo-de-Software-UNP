package com.desarrollo.raffy.business.services;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RaffleRepository;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;

@Service

public class RaffleService {
    
    @Autowired
    private RaffleRepository raffleRepository;

    @Transactional
    public Raffle create(Raffle aRaffle, RegisteredUser aCreator) {
        try {
            aRaffle.setStartDate(LocalDate.now());
            aRaffle.setStatusEvent(StatusEvent.OPEN);
            aRaffle.setCreator(aCreator);
            return raffleRepository.save(aRaffle);
        }
        catch (Exception e) {
            throw new RuntimeException("Error al crear la rifa " + e.getStackTrace());
        }
    }

    @Transactional
    public Raffle update(Raffle aRaffle) {
        try {
            return raffleRepository.save(aRaffle);
        }
        catch (Exception e) {
            throw new RuntimeException("Error al actualizar la rifa " + e.getStackTrace());
        }
    }
}
