package com.example.travel.itinerary.controller;

import org.springframework.web.bind.annotation.*;

import com.example.travel.itinerary.model.Location;
import com.example.travel.itinerary.service.LocationService;

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
