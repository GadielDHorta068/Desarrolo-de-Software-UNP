package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private EventsService eventsService;

    public List<RaffleNumber> findRaffleNumbersById(Long aRaffleId) {
        Events selectedRaffle = eventsService.getById(aRaffleId);
        if (!(selectedRaffle instanceof Raffle)) {
            throw new IllegalArgumentException("Este metodo es solo eventos de tipo rifa");
        }
        List<RaffleNumber> result = raffleNumRepository.findNumbersById(aRaffleId);

        return result;
    }

    public List<Integer> findSoldNumbersById(Long aRaffleId) {
        Events selectedRaffle = eventsService.getById(aRaffleId);
        if (!(selectedRaffle instanceof Raffle)) {
            throw new IllegalArgumentException("Este metodo es solo eventos de tipo rifa");            
        }
        List<Integer> result = raffleNumRepository.findsoldNumbersById(aRaffleId);
        
        return result;
    }

    // agregar metodo para busqueda de participantes
    public List<User> findRaffleOwnersByRaffleId(Long aRaffleId) {
        try {
            return raffleNumRepository.findParticipantsByEventId(aRaffleId);
        }
        catch (Exception e) {
            throw new RuntimeException("Error al obtener los duenios de rifas " + aRaffleId, e);
        }
    }

    @Transactional
    public List<RaffleNumber> createRaffleNumbers(Raffle aRaffle, User aUser, List<Integer> someNumbers) {
        List<RaffleNumber> result = new ArrayList<>();
        for (int i = 0; i < someNumbers.size(); i++) {
            int numberToBuy = someNumbers.get(i);
            if (!raffleNumRepository.existsByRaffleAndNumber(aRaffle, numberToBuy)) {
                RaffleNumber newNumber = new RaffleNumber(aRaffle, aUser, numberToBuy);
                raffleNumRepository.save(newNumber);
                result.add(newNumber);
            }
            else {
                // Remplazar por una excepcion mejor
                throw new IllegalArgumentException("Estas intentando comprar un numero que ya tiene dueño");
            }
        }
        return result;
    }

    @Transactional
    public RaffleNumber save(RaffleNumber aRaffleNumber) {
        return raffleNumRepository.save(aRaffleNumber);
    }
}
