package com.example.travel.itinerary.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;           // 작성자
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

    // 최종 편집된 일정(JSON)
    @Column(columnDefinition = "TEXT")
    private String scheduleJson;

    // 모달에서 입력받은 정보들
    private String planName;         // 최종 여행 이름
    private String planPhoto;        // 사진 URL
    private String planDescription;  // 여행 설명

    // 생성 시각
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
