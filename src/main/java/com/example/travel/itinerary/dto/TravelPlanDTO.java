package com.example.travel.itinerary.dto;

import com.example.travel.itinerary.model.TravelPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanDTO {

    private Long planId;

    @NotBlank(message = "출발지를 입력해주세요")
    private String departLocation;

    @NotBlank(message = "출발 날짜를 입력해주세요")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 YYYY-MM-DD여야 합니다")
    private String startDate;

    @NotBlank(message = "종료 날짜를 입력해주세요")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 YYYY-MM-DD여야 합니다")
    private String endDate;

    @NotBlank(message = "교통 수단을 선택해주세요")
    private String transportation;

    @NotBlank(message = "목적지를 입력해주세요")
    private String destination;

    @NotBlank(message = "여행 목적을 선택해주세요")
    private String purpose;

    @Min(value = 0, message = "예산은 0 이상이어야 합니다")
    private int budget;

    private String density;
    private String hotelLocation;
    private String travelMode;
    private String itinerary;
    private String userId;
    private boolean isCompleted;

    // TravelPlanDTO -> TravelPlan 변환 메서드
    public TravelPlan toEntity() {
        TravelPlan travelPlan = new TravelPlan();
        travelPlan.setPlanId(this.planId);
        travelPlan.setDepartLocation(this.departLocation);
        travelPlan.setStartDate(this.startDate);
        travelPlan.setEndDate(this.endDate);
        travelPlan.setTransportation(this.transportation);
        travelPlan.setDestination(this.destination);
        travelPlan.setPurpose(this.purpose);
        travelPlan.setBudget(this.budget);
        travelPlan.setDensity(this.density);
        travelPlan.setHotelLocation(this.hotelLocation);
        travelPlan.setTravelMode(this.travelMode);
        travelPlan.setItinerary(this.itinerary);
        travelPlan.setUserId(this.userId);
        travelPlan.setCompleted(this.isCompleted);
        return travelPlan;
    }

    // TravelPlan -> TravelPlanDTO 변환 메서드
    public static TravelPlanDTO fromEntity(TravelPlan travelPlan) {
        return TravelPlanDTO.builder()
                .planId(travelPlan.getPlanId())
                .departLocation(travelPlan.getDepartLocation())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .transportation(travelPlan.getTransportation())
                .destination(travelPlan.getDestination())
                .purpose(travelPlan.getPurpose())
                .budget(travelPlan.getBudget())
                .density(travelPlan.getDensity())
                .hotelLocation(travelPlan.getHotelLocation())
                .travelMode(travelPlan.getTravelMode())
                .itinerary(travelPlan.getItinerary())
                .userId(travelPlan.getUserId())
                .isCompleted(travelPlan.isCompleted())
                .build();
    }
}