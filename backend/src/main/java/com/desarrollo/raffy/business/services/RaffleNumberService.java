package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.User;

import java.util.Map;
import java.util.HashMap;

@Service

public class RaffleNumberService {

    @Autowired
    private RaffleNumberRepository raffleNumRepository;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EvolutionService evolutionService;

    @Value("${evolution.defaultInstance:raffy}")
    private String defaultEvolutionInstance;

    private void sendWhatsAppText(String number, String text) {
        try {
            if (number == null || number.isBlank() || text == null || text.isBlank()) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("number", number);
            payload.put("text", text);
            evolutionService.sendText(defaultEvolutionInstance, payload);
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando WhatsApp: " + e.getMessage());
        }
    }

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

        // Construimos datos comunes (correo y WhatsApp)
        String buyerName = (aUser.getName() != null ? aUser.getName() : "") +
                           (aUser.getSurname() != null ? (" " + aUser.getSurname()) : "");
        List<Integer> purchasedNumbers = result.stream()
            .map(RaffleNumber::getNumber)
            .sorted()
            .collect(Collectors.toList());

        // Enviar correo de confirmación
        try {
            emailService.sendRaffleNumbersPurchasedEmail(
                aUser.getEmail(),
                buyerName.trim().isEmpty() ? (aUser.getEmail() != null ? aUser.getEmail() : "Usuario") : buyerName.trim(),
                aRaffle.getId(),
                aRaffle.getTitle(),
                aRaffle.getPriceOfNumber(),
                purchasedNumbers
            );
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando correo de confirmación de números de rifa: " + e.getMessage());
            e.printStackTrace();
        }

        // Enviar WhatsApp de confirmación de compra
        try {
            String numbersText = purchasedNumbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            String displayName = buyerName.trim().isEmpty() ? (aUser.getEmail() != null ? aUser.getEmail() : "Usuario") : buyerName.trim();
            String msg = "Hola " + displayName + ", confirmamos tu compra de números para la rifa '" + aRaffle.getTitle() + "'. "
                       + "Precio por número: $" + String.format("%.2f", aRaffle.getPriceOfNumber()) + ". "
                       + "Números adquiridos: " + numbersText + ". ¡Gracias por participar!";
            sendWhatsAppText(aUser.getCellphone(), msg);
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando WhatsApp de confirmación de compra: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public RaffleNumber save(RaffleNumber aRaffleNumber) {
        return raffleNumRepository.save(aRaffleNumber);
    }
}
