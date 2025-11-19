package com.desarrollo.raffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantRequestDTO {
    private UserDTO user;
    private GuessProgressDTO guessProgress;
}
