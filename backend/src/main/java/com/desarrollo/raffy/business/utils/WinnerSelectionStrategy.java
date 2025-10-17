package com.desarrollo.raffy.business.utils;

import java.util.List;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;

public interface WinnerSelectionStrategy<T> {
    boolean supports(EventTypes eventTypes);
    void selectWinners(Events event, List<T> participants);
}

