package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.Events;

@Service
public class FeaturedEventsService {
    
    @Autowired
    private EventsRepository eventsRepository;

    @Transactional(readOnly = true)
    public List<Events> getFeaturedEvents(EventTypes type){
        List<Events> featured = new ArrayList<>();
        Pageable top = PageRequest.of(0,3);

        switch (type) {
            case GIVEAWAYS -> featured.addAll(
                eventsRepository
                .findTopGiveawaysByParticipants(top)
                .stream()
                .map(obj -> (Events) obj[0])
                .toList()
            );
            case RAFFLES -> featured.addAll(
                eventsRepository
                .findTopRafflesByParticipants(top)
                .stream()
                .map(obj -> (Events) obj[0])
                .toList()
            );
            case GUESSING_CONTEST -> featured.addAll(
                eventsRepository
                .findTopGuessingByParticipants(top)
                .stream()
                .map(obj -> (Events) obj[0])
                .toList()
            );
        }

        if(featured.size() < 3){
            List<Events> recents = eventsRepository.findRecentByType(type, top);
            for(Events event : recents){
                if(featured.size() >= 3) break;
                if (!featured.contains(event)) featured.add(event); 
            }
        }

        return featured;
    }
}
