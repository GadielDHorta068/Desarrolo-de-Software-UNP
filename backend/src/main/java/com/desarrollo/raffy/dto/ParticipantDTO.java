package com.desarrollo.raffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDTO {
    private Long participantId;
    private String name;
    private String surname;
    private short position;
    private Long eventId;
    private String eventTitle;
}
