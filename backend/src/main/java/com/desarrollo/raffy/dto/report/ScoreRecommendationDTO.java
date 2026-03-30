package com.desarrollo.raffy.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ScoreRecommendationDTO {
    
    private int score;
    private String recommendation;
}
