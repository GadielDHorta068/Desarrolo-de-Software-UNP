package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import com.desarrollo.raffy.business.repository.GiveawaysRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.utils.GiveawayWinnerStrategy;
import com.desarrollo.raffy.business.utils.WinnerStrategyFactory;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Participant;
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
    public Giveaways create(Giveaways giveaways){
        try {
            if(giveawaysRepository.existsByTitle(giveaways.getTitle())) {
                throw new RuntimeException("El sorteo con título " + giveaways.getTitle() + " ya existe");
            }
            giveaways.setStartDate(LocalDate.now());
            giveaways.setStatusEvent(StatusEvent.OPEN);
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
    public Giveaways update(Giveaways giveaways){
        try {
            return giveawaysRepository.save(giveaways);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el sorteo " + e.getStackTrace());
        }
    }

    /**
     * Método para finalizar un sorteo
     * Si la fecha actual es igual a la fecha de fin del sorteo
     * se cambia el estado del sorteo a CLOSED
     * @param id
     */
    public boolean finalizedGiveaway(Long id){
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
     * Método para seleccionar los ganadores de un sorteo
     * Se utiliza la estrategia de selección de ganadores
     * @param id
     */
    public void selectWinners(Long id){
        try {
            Giveaways giveaway = giveawaysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sorteo no encontrado"));
            var strategy = winnerStrategyFactory.getStrategy(giveaway.getEventType());
            //List<Participant> participants = participantRepository.findByGiveawayId(giveaway.getId());
            //strategy.selectWinners(giveaway, participants);
            giveawaysRepository.save(giveaway);
        } catch (Exception e) {
            throw new RuntimeException("Error al seleccionar los ganadores del sorteo " + e.getStackTrace());
        }
    }
}

