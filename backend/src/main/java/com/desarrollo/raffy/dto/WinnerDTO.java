package com.desarrollo.raffy.dto;

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
}
