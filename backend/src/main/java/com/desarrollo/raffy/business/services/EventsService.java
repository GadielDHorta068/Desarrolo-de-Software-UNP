package com.desarrollo.raffy.business.services;

import java.time.LocalDate;
//Devolver los errores correspondientes
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.util.ImageUtils;

import org.modelmapper.ModelMapper;
import com.desarrollo.raffy.dto.EventSummaryDTO;
import com.desarrollo.raffy.dto.GiveawaysDTO;
import com.desarrollo.raffy.dto.GuessingContestDTO;

@Service
public class EventsService {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ParticipantRepository participantRepository;

    @Transactional
    public <T extends Events> T create(T event, Long idUser) {
        // Validar que no exista un evento con el mismo título
        if(eventsRepository.existsByTitle(event.getTitle())){
            throw new IllegalArgumentException("Ya existe un sorteo con el título: "+ event.getTitle());
        }
        Optional<RegisteredUser> creator = registeredUserRepository.findById(idUser);        
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
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al finalizar el sorteo " + e.getStackTrace());
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
        
        return dto;
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

    public List<EventSummaryDTO> getActiveEventSummaries(){
        return eventsRepository.findActiveEvents().stream()
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
    
    public List<Events> getActiveEvents() {
        try {
            List<Events> events = eventsRepository.findActiveEvents();
            return events != null ? events : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
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
}
