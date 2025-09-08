package com.desarrollo.raffy.business.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.desarrollo.raffy.model.EventTypes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WinnerStrategyFactory {

    private final List<WinnerSelectionStrategy> strategies;

    public WinnerSelectionStrategy getStrategy(EventTypes eventType){
        return strategies.stream()
        .filter(s -> s.supports(eventType))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "No funciona el strategy para el tipo de evento: " + eventType
        ));
    }
}
