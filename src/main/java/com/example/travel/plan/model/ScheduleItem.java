package com.example.travel.plan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;
    private String departureTime;
    private String category;
    private String location;
    private String duration;
    private String cost;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private TravelPlan travelPlan;
}
