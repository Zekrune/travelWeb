package com.example.travel.plan.controller;

import com.example.travel.plan.model.Location;
import com.example.travel.plan.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public List<Location> searchLocations(@RequestParam(name = "query") String query) {
        return locationService.searchLocations(query);
    }
}
