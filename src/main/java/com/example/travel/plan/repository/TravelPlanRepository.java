package com.example.travel.plan.repository;

import com.example.travel.plan.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, String> {
}
