package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.business.services.RegionService;
import com.desarrollo.raffy.model.Region;

@Controller
@RequestMapping("/region")

public class RegionController {
    @Autowired
    private RegionService regionService;
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllRegions() {
        List<String> result = regionService.findAllRegionNames();
        return Response.ok(result, "ok");
    }
}
