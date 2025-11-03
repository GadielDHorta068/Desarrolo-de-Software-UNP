package com.desarrollo.raffy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor

public class RaffleParticipantDTO {
    private String name;
    private String surname;
    private String email;
    private int number;
    private short position;
}
