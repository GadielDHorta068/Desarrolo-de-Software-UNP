package com.desarrollo.raffy.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desarrollo.raffy.model.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
    
    @Query("SELECT r FROM Region r")
    public List<Region> findAllRegions();

    @Query("SELECT r FROM Region r WHERE r.regionType != 'República'")
    public List<Region> findRegionsWichAreNotCountrys();

    @Query(
        value = 
            "SELECT CASE "
                + "WHEN ST_Contains(event_reg.geom, user_reg.geom) THEN true "
                + "ELSE false "
            + "END "
            + "FROM region event_reg, region user_reg "
            + "WHERE event_reg.id = :eventGeomId AND user_reg.id = :userGeomId", 
        nativeQuery = true
    )
    public boolean isUserRegionInsideEventRegion(@Param("eventGeomId") Long eventGeomId, @Param("userGeomId") Long userGeomId);
}