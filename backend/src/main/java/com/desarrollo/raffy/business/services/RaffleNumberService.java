package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.User;

@Service

public class RaffleNumberService {

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
    public List<RaffleNumber> createRaffleNumbers(Events aRaffle, User aUser, List<Integer> someNumbers) {
        List<RaffleNumber> result = new ArrayList<>();
        for (int i = 0; i < someNumbers.size(); i++) {
            RaffleNumber newNumber = new RaffleNumber(aRaffle, aUser, someNumbers.get(i));
            raffleNumRepository.save(newNumber);
            result.add(newNumber);
        }
        return result;
    }

    @Transactional
    public RaffleNumber save(RaffleNumber aRaffleNumber) {
        return raffleNumRepository.save(aRaffleNumber);
    }
}
