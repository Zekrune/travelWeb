package com.example.travel.plan.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

@Data
@Entity
public class TravelPlan {
    @Id
    private String planId;

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

    // 여행 일정(예: GPT에서 생성된 JSON 형식의 itinerary)
    @Lob
    private String itinerary;

    // 기본 생성자
    public TravelPlan() {
    }

    // 추가: is_completed 필드
    private boolean isCompleted = false; // 기본값 false

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

}
