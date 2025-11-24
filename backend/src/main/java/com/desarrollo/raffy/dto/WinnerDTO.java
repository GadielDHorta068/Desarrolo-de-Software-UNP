package com.desarrollo.raffy.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinnerDTO {
    private Long participantId;
    private String name;
    private String surname;
    private short position;
    private String email;
    private String phone;
    private Long eventId;
    private String eventTitle;
    private Integer raffleNumber; // Número de la rifa (solo para eventos tipo raffle)

    // Campos para eventos de tipo guessing-Contest
    private int attemptCount;
    private String numbersTried;
    private LocalDateTime attemptTime;
    private Long durationSeconds;
}
