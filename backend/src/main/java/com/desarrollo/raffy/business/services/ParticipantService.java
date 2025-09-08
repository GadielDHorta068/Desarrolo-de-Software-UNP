package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.business.repository.ParticipantRepository;

@Service

public class ParticipantService {

    @Autowired
    ParticipantRepository participantRepository;

    
}
