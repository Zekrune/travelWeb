package com.example.travel.itinerary.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id", unique = true, nullable = false)
    private Long planId;

    // 출발/도착/기간/예산 등등 (GPT 생성 시 필요한 정보)
    private String departLocation;
    private String startDate;
    private String endDate;
    private String transportation;
    private String destination;
    private String purpose;
    private int budget;
    private String density;
    private String hotelLocation;
    private String travelMode;

    // GPT가 생성한 일정(JSON)
    @Column(columnDefinition = "TEXT")
    private String itinerary;

    // 작성자
    private String userId;

    // 생성 시각
    private LocalDateTime createdAt;

    // 편집(확정) 완료 여부
    private boolean isCompleted = false;

    @PrePersist
    public void onPrePersist() {
        createdAt = LocalDateTime.now();
    }
}
