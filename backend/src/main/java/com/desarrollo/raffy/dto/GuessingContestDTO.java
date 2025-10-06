package com.desarrollo.raffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GuessingContestDTO extends EventSummaryDTO{
    private int minValue;
    private int maxValue;
    private int maxAttempts;
}
