package com.desarrollo.raffy.business.utils;

import java.util.List;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Participant;

public interface WinnerSelectionStrategy {
    boolean supports(EventTypes eventTypes);
    void selectWinners(Events event, List<Participant> participants);
}

