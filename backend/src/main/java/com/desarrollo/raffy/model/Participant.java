package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participants",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
    }
)

@Setter
@Getter
@NoArgsConstructor

public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User participant;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    // position: 0 = no ganador, >0 = lugar en el ranking
    private short position = 0;
    
    public Participant(User aUser, Giveaways aGiveaways) {
        this.setParticipant(aUser);
        this.setEvent(aGiveaways);
        this.setPosition((short)0);
    }
}
