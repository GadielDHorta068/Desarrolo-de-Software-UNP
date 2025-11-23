package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.dto.RaffleParticipantDTO;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.Payment;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.auditlog.AuditActionType;

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

    @Autowired
    private AuditLogsService auditLogsService;

    @Value("${evolution.defaultInstance:raffy}")
    private String defaultEvolutionInstance;

    @org.springframework.scheduling.annotation.Async
    public void sendWhatsAppText(String number, String text) {
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

    // recupera una instancia de raffleNumber por el nro y el evento
    public RaffleNumber findRaffleNumberByEventIdAndNumber(int number, Long aEventId) {
        Events selectedRaffle = eventsService.getById(aEventId);
        if (!(selectedRaffle instanceof Raffle)) {
            throw new IllegalArgumentException("Este metodo es solo eventos de tipo rifa");
        }
        RaffleNumber result = raffleNumRepository.findNumberByEventIdAndNumber(aEventId, number);

        return result;
    }

    public List<RaffleParticipantDTO> findRaffleNumbersById(Long aRaffleId, String aRequesterEmail) {
        Events selectedRaffle = eventsService.getById(aRaffleId);
        if (!(selectedRaffle instanceof Raffle)) {
            throw new IllegalArgumentException("Este metodo es solo eventos de tipo rifa");
        }
        List<RaffleNumber> raffleNumbers = raffleNumRepository.findNumbersById(aRaffleId)
            .stream()
            .sorted(Comparator.comparing(rn -> rn.getNumberOwner().getEmail()))
            .toList();
        List<RaffleParticipantDTO> result = new ArrayList<>();
        System.out.println("CREATOR EMAIL: [" + selectedRaffle.getCreator().getEmail() + "]");
        System.out.println("REQUESTER EMAIL: [" + aRequesterEmail + "]");
        System.out.println("IGUALES? " + selectedRaffle.getCreator().getEmail().equals(aRequesterEmail));

        for (RaffleNumber rn : raffleNumbers) {
            RaffleParticipantDTO participant = new RaffleParticipantDTO();
            participant.setName(rn.getNumberOwner().getName());
            participant.setSurname(rn.getNumberOwner().getSurname());
            if (selectedRaffle.getCreator().getEmail().equals(aRequesterEmail)) {
                participant.setEmail(rn.getNumberOwner().getEmail());
            }
            else {
                participant.setEmail(censorEmail(rn.getNumberOwner().getEmail()));
            }
            participant.setNumber(rn.getNumber());
            participant.setPosition(rn.getPosition());
            result.add(participant);
        }
        return result;    
    }

    private String censorEmail(String anEmail) {
        int atIndex = anEmail.indexOf('@');
        if (atIndex <= 3) {
            return anEmail.substring(0, atIndex) + "****" + anEmail.substring(atIndex);
        }
        return anEmail.substring(0, atIndex) + "****" + anEmail.substring(atIndex);
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

    // @Transactional
    // public List<RaffleNumber> createRaffleNumbers(Raffle aRaffle, User aUser, List<Integer> someNumbers) {
    //     List<RaffleNumber> result = new ArrayList<>();
    //     for (int i = 0; i < someNumbers.size(); i++) {
    //         int numberToBuy = someNumbers.get(i);
    //         // controlamos que existan y que tengan un pago en proceso asociado
    //         if (!raffleNumRepository.existsByRaffleAndNumber(aRaffle, numberToBuy)) {
    //             RaffleNumber newNumber = new RaffleNumber(aRaffle, aUser, numberToBuy, null);
    //             raffleNumRepository.save(newNumber);
    //             result.add(newNumber);
    //         }
    //         else {
    //             auditLogsService.logAction(
    //                 aRaffle.getId(), 
    //                 aUser.getName() + " " + aUser.getSurname(), 
    //                 AuditActionType.NUMBER_PURCHASED_FAILED, 
    //                 String.format("Falla al comprar número."));
                
    //             throw new IllegalArgumentException("Estas intentando comprar un numero que ya tiene dueño");
    //         }
    //     }

    //     // Construimos datos comunes (correo y WhatsApp)
    //     // String buyerName = (aUser.getName() != null ? aUser.getName() : "") +
    //     //                    (aUser.getSurname() != null ? (" " + aUser.getSurname()) : "");
    //     // List<Integer> purchasedNumbers = result.stream()
    //     //     .map(RaffleNumber::getNumber)
    //     //     .sorted()
    //     //     .collect(Collectors.toList());

    //     // Enviar correo de confirmación
    //     // try {
    //     //     emailService.sendRaffleNumbersPurchasedEmail(
    //     //         aUser.getEmail(),
    //     //         buyerName.trim().isEmpty() ? (aUser.getEmail() != null ? aUser.getEmail() : "Usuario") : buyerName.trim(),
    //     //         aRaffle.getId(),
    //     //         aRaffle.getTitle(),
    //     //         aRaffle.getPriceOfNumber(),
    //     //         purchasedNumbers
    //     //     );
    //     // } catch (Exception e) {
    //     //     System.err.println("⚠️ Error enviando correo de confirmación de números de rifa: " + e.getMessage());
    //     //     e.printStackTrace();
    //     // }

    //     // Enviar WhatsApp de confirmación de compra
    //     // try {
    //     //     String numbersText = purchasedNumbers.stream()
    //     //         .map(String::valueOf)
    //     //         .collect(Collectors.joining(", "));
    //     //     String displayName = buyerName.trim().isEmpty() ? (aUser.getEmail() != null ? aUser.getEmail() : "Usuario") : buyerName.trim();
    //     //     String msg = "Hola " + displayName + ",\n"
    //     //                + "*Confirmación de compra de números*\n"
    //     //                + "Rifa: _" + aRaffle.getTitle() + "_\n"
    //     //                + "Precio por número: *$" + String.format("%.2f", aRaffle.getPriceOfNumber()) + "*\n"
    //     //                + "Números adquiridos: " + numbersText + "\n"
    //     //                + "_¡Gracias por participar!_";
    //     //     sendWhatsAppText(aUser.getCellphone(), msg);
    //     // } catch (Exception e) {
    //     //     System.err.println("⚠️ Error enviando WhatsApp de confirmación de compra: " + e.getMessage());
    //     // }
    //     //Auditoria
    //     // auditLogsService.logAction(
    //     //         aRaffle.getId(), 
    //     //         aUser.getName() + " " + aUser.getSurname(), 
    //     //         AuditActionType.NUMBER_PURCHASED, 
    //     //         String.format("Números comprados: %s", purchasedNumbers.toString()));
    //     auditLogsService.logAction(
    //                 aRaffle.getId(), 
    //                 aUser.getName() + " " + aUser.getSurname(), 
    //                 AuditActionType.USER_REGISTERED, 
    //                 String.format("El usuario se registró al evento."));
    //     //fin auditoria
    //     return result;
    // }
    @Transactional
    public List<RaffleNumber> createRaffleNumbers(Raffle aRaffle, User aUser, List<Integer> someNumbers) {
        List<RaffleNumber> result = new ArrayList<>();
        for (int i = 0; i < someNumbers.size(); i++) {
            int numberToBuy = someNumbers.get(i);
            // buscamos si existe una ref del numero ya creada
            RaffleNumber number = raffleNumRepository.findNumberByEventIdAndNumber(aRaffle.getId(), numberToBuy);
            if(number == null){
                RaffleNumber newNumber = new RaffleNumber(aRaffle, aUser, numberToBuy, null);
                raffleNumRepository.save(newNumber);
                result.add(newNumber);
            }
            else{
                Set<String> validStatus = Set.of("pending", "approved");
                Payment payNumber = number.getPayment();
                if((payNumber == null) || ((payNumber != null) && !validStatus.contains(payNumber.getStatus()))){
                    result.add(number);
                }
                else {
                    auditLogsService.logAction(
                        aRaffle.getId(), 
                        aUser.getName() + " " + aUser.getSurname(), 
                        AuditActionType.NUMBER_PURCHASED_FAILED, 
                        String.format("Falla al comprar número."));
                    
                    throw new IllegalArgumentException("Estas intentando comprar un numero que ya tiene dueño");
                }
            }
        }

        auditLogsService.logAction(
                    aRaffle.getId(), 
                    aUser.getName() + " " + aUser.getSurname(), 
                    AuditActionType.USER_REGISTERED, 
                    String.format("El usuario se registró al evento."));
        //fin auditoria
        return result;
    }

    @Transactional
    public RaffleNumber save(RaffleNumber aRaffleNumber) {
        return raffleNumRepository.save(aRaffleNumber);
    }
}
