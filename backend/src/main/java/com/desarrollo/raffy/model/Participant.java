package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Participants",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "giveaway_id"})
    } // Asegura q la combinacion usuario+sorteo no se repita 
)

@Setter
@Getter
@NoArgsConstructor

public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "participant_id_seq")
    @SequenceGenerator(name = "participant_id_seq", sequenceName = "participant_idseq", initialValue = 0, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User participant;

    @ManyToOne
    @JoinColumn(name = "giveaway_id", nullable = false)
    private Giveaways giveaway;

    // position: 0 = no ganador, >0 = lugar en el ranking
    private short position = 0;
    
    public Participant(User aUser, Giveaways aGiveaways) {
        this.setParticipant(aUser);
        this.setGiveaway(aGiveaways);
        this.setPosition((short)0);
    }
}
