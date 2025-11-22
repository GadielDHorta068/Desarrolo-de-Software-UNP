package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.GuessStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckGuessNumberDTO {
    
    private GuessStatus status; // Estado en que se encuentra
    private String message;    // Mensaje adicional sobre el estado de la adivinanza
}
