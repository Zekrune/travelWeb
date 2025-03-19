package com.example.travel.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

import com.example.travel.schedule.model.Schedule;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {

    private Long id;
    private String userId;

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

    @NotBlank(message = "일정 정보가 필요합니다")
    private String scheduleJson;

    @NotBlank(message = "여행 이름을 입력해주세요")
    private String planName;

    private String planPhoto;
    private String planDescription;

    private LocalDateTime createdAt;

    // ScheduleDTO -> Schedule 변환 메서드
    public Schedule toEntity() {
        Schedule schedule = new Schedule();
        schedule.setId(this.id);
        schedule.setUserId(this.userId);
        schedule.setDepartLocation(this.departLocation);
        schedule.setStartDate(this.startDate);
        schedule.setEndDate(this.endDate);
        schedule.setTransportation(this.transportation);
        schedule.setDestination(this.destination);
        schedule.setPurpose(this.purpose);
        schedule.setBudget(this.budget);
        schedule.setDensity(this.density);
        schedule.setHotelLocation(this.hotelLocation);
        schedule.setTravelMode(this.travelMode);
        schedule.setScheduleJson(this.scheduleJson);
        schedule.setPlanName(this.planName);
        schedule.setPlanPhoto(this.planPhoto);
        schedule.setPlanDescription(this.planDescription);
        schedule.setCreatedAt(this.createdAt);
        return schedule;
    }

    // Schedule -> ScheduleDTO 변환 메서드
    public static ScheduleDTO fromEntity(Schedule schedule) {
        return ScheduleDTO.builder()
                .id(schedule.getId())
                .userId(schedule.getUserId())
                .departLocation(schedule.getDepartLocation())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .transportation(schedule.getTransportation())
                .destination(schedule.getDestination())
                .purpose(schedule.getPurpose())
                .budget(schedule.getBudget())
                .density(schedule.getDensity())
                .hotelLocation(schedule.getHotelLocation())
                .travelMode(schedule.getTravelMode())
                .scheduleJson(schedule.getScheduleJson())
                .planName(schedule.getPlanName())
                .planPhoto(schedule.getPlanPhoto())
                .planDescription(schedule.getPlanDescription())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}