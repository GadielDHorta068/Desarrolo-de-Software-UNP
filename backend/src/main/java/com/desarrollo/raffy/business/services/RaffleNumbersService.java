package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;

@Service

public class RaffleNumbersService {

    @Autowired
    private RaffleNumberRepository raffleNumRepository;

    @Autowired
    private EventsRepository eventsRepository;

    public List<RaffleNumber> findRaffleNumbersById(Raffle aRaffle) {
        if (!(aRaffle instanceof Raffle)) {
            throw new IllegalArgumentException("Este metodo es solo para asignar numeros de rifa a usuarios")
        }
        List<RaffleNumber> result = raffleNumRepository.findNumbersById(aRaffle.getId());

        return result;
    }

    @Transactional
    public void createRaffleNumbers(Raffle aRaffle) {
        for (int i = 0; i < aRaffle.getQuantityOfNumbers(); i++) {
            RaffleNumber newNumber = new RaffleNumber(i + 1, aRaffle);
            raffleNumRepository.save(newNumber);
        }
    }

    @Transactional
    public RaffleNumber save(RaffleNumber aRaffleNumber) {
        return raffleNumRepository.save(aRaffleNumber);
    }
}
