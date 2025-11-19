package com.desarrollo.raffy.dto;

import lombok.Data;

@Data
public class GuessCheckResponseDTO {
    
    private boolean alreadyParticipated;  // estado que indica si está o no inscripto
    private String message;              // Mensaje que manda de acuerdo al resultado de alreadyParticipated
}
