package com.desarrollo.raffy.business.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.business.repository.CategoriesRepository;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.util.ImageUtils;
import com.desarrollo.raffy.util.OnCreate;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import com.desarrollo.raffy.dto.EventSummaryDTO;
import com.desarrollo.raffy.dto.GiveawaysDTO;
import com.desarrollo.raffy.dto.GuessingContestDTO;
import com.desarrollo.raffy.dto.RaffleDTO;


@Service
@Slf4j
public class EventsService {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ParticipantService participantService;

    @Autowired 
    private EvolutionService evolutionService;

    @Value("${evolution.defaultInstance:raffy}")
    private String defaultEvolutionInstance;

    @Transactional
    @Validated(OnCreate.class)
    public <T extends Events> T create(T event, Long idUser) {
        // Validar que no exista un evento con el mismo título
        if(eventsRepository.existsByTitle(event.getTitle())){
            throw new IllegalArgumentException("Ya existe un sorteo con el título: "+ event.getTitle());
        }
        Optional<RegisteredUser> creator = registeredUserRepository.findById(idUser);
        
        if (creator.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUser);
        }

        event.setCreator(creator.get());
        event.setStatusEvent(StatusEvent.OPEN);
        event.setStartDate(LocalDate.now());
        
        // Solo procesar la imagen si imageBase64 no es null
        if (event.getImageBase64() != null && !event.getImageBase64().trim().isEmpty()) {
            event.setImagen(ImageUtils.base64ToBytes(event.getImageBase64()));
        } else {
            event.setImagen(null);
        }
        
        return eventsRepository.save(event);
    }

    /**
     * Actualiza un evento existente si el usuario es el creador del evento.
     * @param <T>
     * @param idEvent
     * @param event
     * @param idUser
     * @return El evento actualizado.
     * @throws IllegalArgumentException si el evento no existe o el usuario no es el creador.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public <T extends Events> T update(Long idEvent, T event, Long idUser) {

        Events existing = eventsRepository.findById(idEvent)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        RegisteredUser creator = registeredUserRepository.findById(idUser)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!existing.getCreator().getId().equals(creator.getId())) {
               throw new IllegalArgumentException("No tienes permiso para actualizar este evento");
        }
            
        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setCategory(event.getCategory());
        existing.setEndDate(event.getEndDate());
        existing.setWinnersCount(event.getWinnersCount());

        if(event.getImageBase64() != null && !event.getImageBase64().isEmpty()){
        existing.setImagen(ImageUtils.base64ToBytes(event.getImageBase64()));
        } else{
            existing.setImagen(existing.getImagen());
        }

        if(existing instanceof Giveaways && event instanceof Giveaways){
            // No hay campos específicos para actualizar en Giveaways por ahora
        } else if(existing instanceof GuessingContest && event instanceof GuessingContest) {
            GuessingContest existingContest = (GuessingContest) existing;
            GuessingContest newContest = (GuessingContest) event;

            existingContest.setMinValue(newContest.getMinValue());
            existingContest.setMaxValue(newContest.getMaxValue());
            existingContest.setMaxAttempts(newContest.getMaxAttempts());
        }

        return (T) eventsRepository.save(existing);
    }

    ///Método para traer todos los eventos(Giveaways, Raffles, etc) dado el id del creador
    public List<Events> findByEventsCreator(Long IdCreator){

        return eventsRepository.findByCreatorId(IdCreator);
    }

    /**
     * Cierra un evento cambiando su estado a CLOSED.
     * @param idEvent
     * @return true si el evento se cerró correctamente, false si ya estaba cerrado o finalizado.
     * @throws RuntimeException si el evento no se encuentra o hay un error al guardar.
     */
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
            log.error("Error enviando mensaje a {}: {}", number, e.getMessage());
        }
    }

    public boolean closeEvent(Long idEvent){
        try {
            Events event = eventsRepository.findById(idEvent)
                .orElseThrow(() -> new RuntimeException("Sorteo no econtrado"));
            
            // Solo cerrar si no está ya cerrado o finalizado
            if (event.getStatusEvent() == StatusEvent.CLOSED || event.getStatusEvent() == StatusEvent.FINALIZED) {
                return false;
            }
            
            event.setStatusEvent(StatusEvent.CLOSED);
            eventsRepository.save(event);
            
            try {
                int participantsCount = participantRepository.findParticipantsByEventId(event.getId()).size();
                String categoryName = event.getCategory() != null ? event.getCategory().getName() : "";
                String eventTypeText = event.getEventType() != null ? event.getEventType().name() : "EVENTO";
                String msg = "Tu *" + eventTypeText + "* '" + event.getTitle() + "' ha sido *CERRADO*.\n"
                           + "Categoría: _" + categoryName + "_\n"
                           + "Participantes: *" + participantsCount + "*\n"
                           + "Finaliza: " + event.getEndDate() + "\n"
                           + "_Luego podrás finalizar para elegir ganadores._";
                String creatorPhone = event.getCreator() != null ? event.getCreator().getCellphone() : null;
                sendWhatsAppText(creatorPhone, msg);
            } catch (Exception ex) {
                log.warn("No se pudo enviar resumen de cierre: {}", ex.getMessage());
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al finalizar el sorteo " + e.getMessage(), e);
        }
    }

    public EventTypes[] getAllEventTypes() {
        return EventTypes.values();
    }

    public List<EventSummaryDTO> getEventSummariesByCreator(Long creatorId){
        return eventsRepository.findByCreatorId(creatorId).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventSummaryDTO getEventSummaryById(Long id){
        Events event = eventsRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        return toEventSummaryDTO(event);
    }

    public EventSummaryDTO toEventSummaryDTO(Events event) {
        EventSummaryDTO dto;
        if(event instanceof Giveaways){
            dto = modelMapper.map(event, GiveawaysDTO.class);
        } else if(event instanceof GuessingContest){
            dto = modelMapper.map(event, GuessingContestDTO.class);
        } else if(event instanceof Raffle) {
            dto = modelMapper.map(event, RaffleDTO.class);
        } else {
            dto = modelMapper.map(event, EventSummaryDTO.class);
        }

        if (event.getImagen() != null) {
            dto.setImageUrl(ImageUtils.bytesToBase64(event.getImagen()));
        }
        
        // Verificar si el usuario actual está inscrito en el evento
        RegisteredUser currentUser = getCurrentUser();
        if (currentUser != null) {
            boolean isRegistered = participantRepository.existsByParticipantAndEvent(currentUser, event);
            dto.setIsUserRegistered(isRegistered);
        } else {
            dto.setIsUserRegistered(false);
        }
        /* System.out.println("Evento: " + event.getTitle() + " - Categoria: " + 
            (event.getCategory() != null ? event.getCategory().getName() : "NULL")); */

        return dto;
    }

    @Transactional
    public List<?> finalizedEvent(Long eventId){
        Events event = eventsRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        log.info("Finalizando evento: " + event.getTitle() + " con estado: " + event.getStatusEvent());
        
        if (event.getStatusEvent() != StatusEvent.CLOSED) {
            throw new IllegalStateException("El evento debe estar cerrado para poder finalizarse");
        }

        // Cambia el estado del evento a FINALIZED
        event.setStatusEvent(StatusEvent.FINALIZED);

        
        //Delega la selección de ganadores a ParticipantService
        List<?> winners = participantService.runEvents(event);
        
        log.info("Ganadores seleccionados: " + winners.size());
        
        eventsRepository.save(event);
        
        // Enviar mensajes a los ganadores
        for (Object winner : winners) {
            String winnerPhone = null;
            String winnerName = null;

            if (winner instanceof Participant) {
                Participant p = (Participant) winner;
                winnerPhone = p.getParticipant().getCellphone();
                winnerName = p.getParticipant().getName();
            } else if (winner instanceof RaffleNumber) {
                RaffleNumber rn = (RaffleNumber) winner;
                winnerPhone = rn.getNumberOwner().getCellphone();
                winnerName = rn.getNumberOwner().getName();
            }

            if (winnerPhone != null && !winnerPhone.isBlank()) {
                String msg = "¡Felicidades " + winnerName + "! Has ganado en el evento " + event.getTitle() + ".";
                sendWhatsAppText(winnerPhone, msg);
            }
        }
        
        // Enviar resumen al creador del evento
        String creatorPhone = event.getCreator().getCellphone();
        if (creatorPhone != null && !creatorPhone.isBlank()) {
            StringBuilder summary = new StringBuilder();
            summary.append("Resumen del evento '").append(event.getTitle()).append("':\n");
            summary.append("Se han seleccionado ").append(winners.size()).append(" ganadores.\n\n");
            
            for (int i = 0; i < winners.size(); i++) {
                Object winner = winners.get(i);
                String winnerName = null;
                if (winner instanceof Participant) {
                    winnerName = ((Participant) winner).getParticipant().getName();
                } else if (winner instanceof RaffleNumber) {
                    winnerName = ((RaffleNumber) winner).getNumberOwner().getName();
                }
                summary.append(i + 1).append(". ").append(winnerName).append("\n");
            }
            
            sendWhatsAppText(creatorPhone, summary.toString());
        }
        
        return winners;
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad
     * @return RegisteredUser o null si no hay usuario autenticado
     */
    private RegisteredUser getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof RegisteredUser) {
                return (RegisteredUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // Si hay algún error, simplemente retornamos null
        }
        return null;
    }


    public List<EventSummaryDTO> getAllEventSummaries(){
        return eventsRepository.findAllWithDetails().stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByStatus(StatusEvent statusEvent){
        return eventsRepository.findByStatusEvent(statusEvent).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByEventType(EventTypes eventType){
        return eventsRepository.findByEventType(eventType).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByCategoryId(Long categoryId){
        return eventsRepository.findByCategoryId(categoryId).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<EventSummaryDTO> getActiveEventSummaries(
        EventTypes type, 
        String categorie, 
        LocalDate start, 
        LocalDate end, 
        Integer winnerCount,
        StatusEvent statusEvent){
        // Resolver opcionalmente el nombre de categoría a su ID para evitar problemas
        // con funciones de texto sobre tipos binarios y asegurar consulta indexada.
        Long categoryId = null;
        if (categorie != null && !categorie.isBlank()) {
            try {
                // Intentar parsear como ID directo si viene numérico
                categoryId = Long.parseLong(categorie.trim());
            } catch (NumberFormatException nfe) {
                // Si no es numérico, buscar por nombre
                var cat = categoriesRepository.findByName(categorie.trim());
                if (cat != null) categoryId = cat.getId();
            }
        }

        return eventsRepository.findActiveEvents(statusEvent, type, categoryId, start, end, winnerCount)
            .stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByDateRange(LocalDate startDate, LocalDate endDate){
        return eventsRepository.findByDateRange(startDate, endDate).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByStartDate(LocalDate startDate){
        return eventsRepository.findByStartDate(startDate).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByEndDate(LocalDate endDate){
        return eventsRepository.findByEndDate(endDate).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> searchEventSummariesByTitle(String title){
        return eventsRepository.findByTitleContainingIgnoreCase(title).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventSummariesByParticipantId(Long userId){
        return eventsRepository.findByParticipantId(userId).stream()
            .map(this::toEventSummaryDTO)
            .collect(Collectors.toList());
    }

    public boolean delete(Long id) {
        try {
            eventsRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Events> getAll() {
        try {
            List<Events> events = eventsRepository.findAll();
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }       
    }

    // Nuevos métodos para mapear las consultas del repositorio
    
    public List<Events> getByStatusEvent(StatusEvent statusEvent) {
        try {
            List<Events> events = eventsRepository.findByStatusEvent(statusEvent);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByEventType(EventTypes eventType) {
        try {
            List<Events> events = eventsRepository.findByEventType(eventType);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByCategoryId(Long categoryId) {
        try {
            List<Events> events = eventsRepository.findByCategoryId(categoryId);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    /* public List<Events> getActiveEvents() {
        try {
            List<Events> events = eventsRepository.findActiveEvents();
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    } */
    
    public List<Events> getByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<Events> events = eventsRepository.findByDateRange(startDate, endDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByStartDate(LocalDate startDate) {
        try {
            List<Events> events = eventsRepository.findByStartDate(startDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getByEndDate(LocalDate endDate) {
        try {
            List<Events> events = eventsRepository.findByEndDate(endDate);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public boolean existsByTitle(String title) {
        try {
            return eventsRepository.existsByTitle(title);
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<Events> searchByTitle(String title) {
        try {
            List<Events> events = eventsRepository.findByTitleContainingIgnoreCase(title);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Events> getEventsByParticipantId(Long userId) {
        try {
            List<Events> events = eventsRepository.findByParticipantId(userId);
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Events getById(Long id) {
        try {
            return eventsRepository.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getUsersParticipantsByEventId(Long aEventId) {
        try {
            return eventsRepository.findParticipantsByEventId(aEventId);
        }
        catch (Exception e) {
            throw new RuntimeException("Error al obtener los participantes del evento " + aEventId, e);
        }
    }
}
