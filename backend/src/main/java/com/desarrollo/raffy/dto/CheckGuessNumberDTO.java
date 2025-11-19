package com.desarrollo.raffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckGuessNumberDTO {
    
    private int guessedNumber; // Número que el usuario está adivinando
    private String message;    // Mensaje adicional sobre el estado de la adivinanza
}
