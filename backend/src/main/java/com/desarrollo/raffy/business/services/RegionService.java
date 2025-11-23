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

    public List<Region> findAllRegionNames() {
        return regionRepository.findAllRegions();
    }

    public List<Region> findRegionNamesWichAreNotCountrys() {
        return regionRepository.findRegionsWichAreNotCountrys();
    }

    public boolean isUserRegionInsideEventRegion(Long eventGeomId, Long userGeomId) {
        return regionRepository.isUserRegionInsideEventRegion(eventGeomId, userGeomId);
    }
}
