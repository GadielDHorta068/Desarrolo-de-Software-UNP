package com.desarrollo.raffy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor

@Entity @Table(name = "raffle")
@Inheritance(strategy = InheritanceType.JOINED)

public class Raffle extends Events {
    
    @NotNull(message = "La cantidad de numeros es obligatoria")
    @Column(name = "quentity_of_numbers")
    private int quantityOfNumbers;

    @NotNull(message = "El precio de la rifa es obligatorio")
    @Column(name = "price_of_number")
    private double priceOfNumber;

}
