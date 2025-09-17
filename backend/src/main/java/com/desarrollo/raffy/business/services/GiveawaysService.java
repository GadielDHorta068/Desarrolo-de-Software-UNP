package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import com.desarrollo.raffy.business.repository.GiveawaysRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.utils.GiveawayWinnerStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.StatusEvent;

@Service
public class GiveawaysService {
    
    @Autowired
    private GiveawaysRepository giveawaysRepository;

    @Autowired
    private GiveawayWinnerStrategy giveawayWinnerStrategy;

    @Autowired
    private WinnerStrategyFactory winnerStrategyFactory;

    @Autowired
    private ParticipantRepository participantRepository;

    /**
     * Método para crear un sorteo
     * Se verifica que no exista un sorteo con el mismo título
     * Se establece la fecha de inicio como la fecha actual
     * Se establece el estado del sorteo como OPEN
     * @param giveaways
     * @return
     */
    @Transactional
    public Giveaways create(Giveaways giveaways, RegisteredUser creator){
        try {
            giveaways.setStartDate(LocalDate.now());
            giveaways.setStatusEvent(StatusEvent.OPEN);
            giveaways.setCreator(creator);
            return giveawaysRepository.save(giveaways);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el sorteo " + e.getStackTrace());
        }
    }

    /**
     * Método para actializar un sorteo
     * Se utiliza el método save del repositorio
     * @param giveaways
     * @return
     */
    @Transactional
    public Giveaways update(Giveaways giveaways){
        try {
            return giveawaysRepository.save(giveaways);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el sorteo " + e.getStackTrace());
        }
    }

    /**
     * Método para cerrar un sorteo
     * Si la fecha actual es igual a la fecha de fin del sorteo
     * se cambia el estado del sorteo a CLOSED
     * @param id
     */
    @Transactional
    public boolean closedGiveaway(Long id){
        try {
            Giveaways giveaway = giveawaysRepository.findById(id).orElseThrow(() -> new RuntimeException("Sorteo no encontrado"));
            if(LocalDate.now().isEqual(giveaway.getEndDate())){
                giveaway.setStatusEvent(StatusEvent.CLOSED);
                giveawaysRepository.save(giveaway);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al finalizar el sorteo " + e.getStackTrace());
        }
    }

    /**
     * Método para finalizar un sorteo
     * Si el estado del sorteo es CLOSED
     * se cambia el estado del sorteo a FINALIZED
     * y selecciona los ganadores
     * @param id
     * @return
     */
    @Transactional
    public boolean finalizedGiveaway(Long id){
        try {
            Giveaways giveaway = giveawaysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sorteo no encontrado"));
            
            if(giveaway.getStatusEvent() == StatusEvent.CLOSED){
                selectWinners(id);
                
                giveaway.setStatusEvent(StatusEvent.FINALIZED);
                
                giveawaysRepository.save(giveaway);
                
                return true;
            }

            return false;

        } catch (Exception e) {
            throw new RuntimeException("Error al finalizar el sorteo " + e.getStackTrace());
        }
    }

    /**
     * Método para seleccionar los ganadores de un sorteo
     * Se utiliza la estrategia de selección de ganadores
     * @param id
     */
    private void selectWinners(Long id){
        try {
            Giveaways giveaway = giveawaysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sorteo no encontrado"));
            
            // Obtener la estrategia adecuada
            var strategy = winnerStrategyFactory.getStrategy(giveaway.getEventType());
            List<Participant> participants = participantRepository.findParticipantsByEventId(id);
            strategy.selectWinners(giveaway, participants);
            
            //Permitir cambios, ganadores con position > 0
            giveawaysRepository.save(giveaway);
        } catch (Exception e) {
            throw new RuntimeException("Error al seleccionar los ganadores del sorteo " + e.getStackTrace());
        }
    }

    @Transactional
    public List<Giveaways> findByGiveawaysCategoryId(Long categoryId){
        try {
            if((categoryId == null) || (categoryId <= 0)) {
                throw new IllegalArgumentException("El ID de la categoría debe ser un número positivo");
            }
            return giveawaysRepository.findByCategoryId(categoryId);
        } catch(Exception e) {
            throw new RuntimeException("Error al buscar sorteos por categoría " + e.getStackTrace());
        }
    }

    @Transactional
    public List<Giveaways> findByStatusGiveaways(StatusEvent statusEvent){
        try {
            if(statusEvent == null) {
                throw new IllegalArgumentException("El estado del sorteo no debe ser nulo");
        }
            return giveawaysRepository.findByStatusEvent(statusEvent);
        } catch(Exception e) {
            throw new RuntimeException("Error al buscar sorteos por estado " + e.getStackTrace());
        }
    }

    @Transactional
    public List<Giveaways> findByActiGiveaways(){
        try {
            return giveawaysRepository.findByActiveEvent();
        } catch(Exception e) {
            throw new RuntimeException("Error al buscar sorteos activos " + e.getStackTrace());
        }
    }

    @Transactional
    public List<Giveaways> findByDateRangeGiveaways(LocalDate startDate, LocalDate endDate){
        try {
            if(startDate == null || endDate == null) {
                throw new IllegalArgumentException("Las fechas no deben ser nulas");
            }
            if(endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
            }
            return giveawaysRepository.findByDateRange(startDate, endDate);
        } catch(Exception e) {
            throw new RuntimeException("Error al buscar sorteos por rango de fechas " + e.getStackTrace());
        }
    }
}

