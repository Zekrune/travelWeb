package com.example.travel.itinerary.service;

import org.springframework.stereotype.Service;

import com.example.travel.itinerary.model.Location;
import com.example.travel.itinerary.repository.LocationRepository;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> searchLocations(String query) {
        return locationRepository.findByCityContainingIgnoreCase(query);
    }
}
