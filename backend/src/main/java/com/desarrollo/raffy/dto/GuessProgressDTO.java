package com.desarrollo.raffy.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessProgressDTO {

    private int attemptCount; // Cantidad total de intentos
    private String numbersTried; // Lista de números separados por coma
    private boolean hasWon; // Si adivinó o no
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime lastAttemptTime; // Fecha y hora del intento
    private Long durationSeconds; // Duración total en segundos
}
