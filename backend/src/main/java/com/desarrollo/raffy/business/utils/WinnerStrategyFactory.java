package com.desarrollo.raffy.business.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.desarrollo.raffy.model.EventTypes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WinnerStrategyFactory {

    private final List<WinnerSelectionStrategy<?>> strategies;

    @SuppressWarnings("unchecked")
    public <T>WinnerSelectionStrategy<T> getStrategy(EventTypes eventType){
        return (WinnerSelectionStrategy<T>) strategies.stream()
                .filter(s -> s.supports(eventType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No funciona el strategy para el tipo de evento: " + eventType
                ));
    }
}
