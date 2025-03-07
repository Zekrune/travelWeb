package com.example.travel.plan.service;

import com.example.travel.plan.model.Location;
import com.example.travel.plan.repository.LocationRepository;
import org.springframework.stereotype.Service;

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
