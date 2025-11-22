package com.desarrollo.raffy.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.business.repository.RegionRepository;
import com.desarrollo.raffy.model.Region;

@Service

public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    public List<String> findAllRegionNames() {
    
        return regionRepository.findAllRegionNames();
    }
}
