package com.example.travel.itinerary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.travel.itinerary.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByCityContainingIgnoreCase(String query);
}
