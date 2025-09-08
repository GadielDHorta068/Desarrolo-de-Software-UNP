package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "giveaways")
@Getter @Setter
public class Giveaways extends Events {
    
    public Giveaways() {
        super();
    }

    //Cuantos ganadores va a tener el sorteo
    private int winnersCount = 0;
}
