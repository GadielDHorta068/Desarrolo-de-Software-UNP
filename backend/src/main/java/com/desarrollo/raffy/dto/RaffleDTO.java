package com.desarrollo.raffy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor 
@SuperBuilder
@EqualsAndHashCode(callSuper = true)

public class RaffleDTO extends EventSummaryDTO {
    private int quantityOfNumbers; 
    private double priceOfNumber;
}
