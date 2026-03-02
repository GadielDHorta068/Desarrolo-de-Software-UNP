package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.ScoreService;
import com.desarrollo.raffy.dto.report.ScoreRecommendationDTO;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/score")
public class ScoreController {
    
    @Autowired
    private ScoreService service;

    @GetMapping("/event/{eventId}/score")
    @Operation(summary = "Score y recomendación", description = "Obtiene el score y recomendación del evento")
    public ResponseEntity<?> getEventScore(@PathVariable Long eventId) {
        try {
            int score = service.calculateScoreEvent(eventId);
            String recommendation = service.getRecommendation(eventId);
            
            return ResponseEntity.ok(new ScoreRecommendationDTO(score, recommendation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
