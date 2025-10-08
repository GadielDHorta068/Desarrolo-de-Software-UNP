package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter
@NoArgsConstructor

@Entity
@Table(name = "raffle_number",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {
            "raffle_id",
            "number"
        })
    }
)

public class RaffleNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El numero debe estar definido")
    private int number;

    @ManyToOne
    @JoinColumn(name = "raffle_id")
    @NotNull(message = "cada numero debe indicar a que rifa pertenece")
    private Events raffle;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "Todo numero de una rifa debe estar asociado a un usuario")
    private User numberOwner;
    
    // position: 0 = no ganador, >0 = lugar en el ranking
    private short position = 0;

    public RaffleNumber(Events aRaffle, User aUser, int aNumber) {
        this.setNumber(aNumber);
        this.setRaffle(aRaffle);
        this.setNumberOwner(aUser);
        this.setPosition((short)0);
    }
    
}
