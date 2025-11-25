package com.desarrollo.raffy.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessProgressDTO {

    private int attemptCount;
    private String numbersTried;
    private boolean hasWon;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime lastAttemptTime;
    private Long durationSeconds;
}
