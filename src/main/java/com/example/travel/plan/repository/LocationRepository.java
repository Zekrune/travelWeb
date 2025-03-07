package com.example.travel.plan.repository;

import com.example.travel.plan.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByCityContainingIgnoreCase(String query);
}
