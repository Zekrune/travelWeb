package com.example.travel.itinerary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel.exception.BadRequestException;
import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.itinerary.dto.TravelPlanDTO;
import com.example.travel.itinerary.model.TravelPlan;
import com.example.travel.itinerary.repository.TravelPlanRepository;
import com.example.travel.schedule.dto.ScheduleDTO;
import com.example.travel.schedule.model.Schedule;
import com.example.travel.schedule.repositroy.ScheduleRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private GPTService gptService;

    /**
     * 사용자의 모든 여행 계획을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TravelPlanDTO> getUserPlans(String userId) {
        List<TravelPlan> plans = travelPlanRepository.findByUserId(userId);
        return plans.stream()
                .map(TravelPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * (1) 사용자 입력 + GPT 생성 → TravelPlan 저장
     */
    @Transactional
    public TravelPlanDTO createPlan(TravelPlanDTO planDTO) {
        validateTravelPlanInput(planDTO);

        // GPT로 일정 JSON 생성
        TravelPlan plan = planDTO.toEntity();
        try {
            String planContent = gptService.generatePlanContent(plan);

            // JSON 형식 검증 추가
            try {
                // JSON 형식 검증
                new ObjectMapper().readTree(planContent);
            } catch (Exception e) {
                throw new BadRequestException("GPT가 생성한 일정이 올바른 JSON 형식이 아닙니다: " + e.getMessage());
            }

            plan.setItinerary(planContent);
        } catch (Exception e) {
            throw new BadRequestException("GPT 일정 생성 중 오류가 발생했습니다: " + e.getMessage());
        }

        // TravelPlan 저장
        TravelPlan saved = travelPlanRepository.save(plan);
        return TravelPlanDTO.fromEntity(saved);
    }

    /**
     * (2) planId로 TravelPlan 조회
     */
    @Transactional(readOnly = true)
    public TravelPlanDTO getPlan(Long planId) {
        TravelPlan travelPlan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPlan", "planId", planId));
        return TravelPlanDTO.fromEntity(travelPlan);
    }

    /**
     * (3) 편집 완료 후 최종 확정 → Schedule 생성
     */
    @Transactional
    public ScheduleDTO confirmPlan(Long planId, String editedItinerary, String userId,
            String planName, String planPhoto, String planDescription) {
        if (editedItinerary == null || editedItinerary.trim().isEmpty()) {
            throw new BadRequestException("편집된 일정 정보가 없습니다.");
        }

        if (planName == null || planName.trim().isEmpty()) {
            throw new BadRequestException("여행 이름은 필수 입력 항목입니다.");
        }

        TravelPlan travelPlan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPlan", "planId", planId));

        // 권한 확인
        if (!travelPlan.getUserId().equals(userId)) {
            throw new BadRequestException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // 이미 완료된 계획인지 확인
        if (travelPlan.isCompleted()) {
            throw new BadRequestException("이미 확정된 여행 계획입니다.");
        }

        Schedule schedule = createScheduleFromTravelPlan(travelPlan, userId, editedItinerary);

        // 추가 정보를 Schedule에 반영
        schedule.setPlanName(planName);
        schedule.setPlanPhoto(planPhoto != null ? planPhoto : "");
        schedule.setPlanDescription(planDescription != null ? planDescription : "");

        // TravelPlan은 완료 상태로 변경
        travelPlan.setCompleted(true);
        travelPlanRepository.save(travelPlan);

        // Schedule 저장
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleDTO.fromEntity(savedSchedule);
    }

    /**
     * (4) 모달에서 받은 추가 정보를 Schedule에 업데이트
     */
    @Transactional
    public ScheduleDTO updateAdditionalInfo(Long scheduleId, String planName, String planPhoto,
            String planDescription) {
        if (planName == null || planName.trim().isEmpty()) {
            throw new BadRequestException("여행 이름은 필수 입력 항목입니다.");
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));

        schedule.setPlanName(planName);
        schedule.setPlanPhoto(planPhoto != null ? planPhoto : schedule.getPlanPhoto());
        schedule.setPlanDescription(planDescription != null ? planDescription : schedule.getPlanDescription());

        Schedule updated = scheduleRepository.save(schedule);
        return ScheduleDTO.fromEntity(updated);
    }

    /**
     * 사용자의 모든 확정된 일정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO> getUserSchedules(String userId) {
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        return schedules.stream()
                .map(ScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * TravelPlan 엔티티에서 Schedule 엔티티를 생성합니다.
     */
    private Schedule createScheduleFromTravelPlan(TravelPlan travelPlan, String userId, String editedItinerary) {
        Schedule schedule = new Schedule();
        schedule.setUserId(userId);
        schedule.setDepartLocation(travelPlan.getDepartLocation());
        schedule.setStartDate(travelPlan.getStartDate());
        schedule.setEndDate(travelPlan.getEndDate());
        schedule.setTransportation(travelPlan.getTransportation());
        schedule.setDestination(travelPlan.getDestination());
        schedule.setPurpose(travelPlan.getPurpose());
        schedule.setBudget(travelPlan.getBudget());
        schedule.setDensity(travelPlan.getDensity());
        schedule.setHotelLocation(travelPlan.getHotelLocation());
        schedule.setTravelMode(travelPlan.getTravelMode());
        schedule.setScheduleJson(editedItinerary);
        return schedule;
    }

    /**
     * 여행 계획 입력값을 검증합니다.
     */
    private void validateTravelPlanInput(TravelPlanDTO planDTO) {
        // 필수 필드 검증
        if (planDTO.getDepartLocation() == null || planDTO.getDepartLocation().trim().isEmpty()) {
            throw new BadRequestException("출발지는 필수 입력 항목입니다.");
        }

        if (planDTO.getDestination() == null || planDTO.getDestination().trim().isEmpty()) {
            throw new BadRequestException("목적지는 필수 입력 항목입니다.");
        }

        if (planDTO.getStartDate() == null || planDTO.getStartDate().trim().isEmpty()) {
            throw new BadRequestException("출발 날짜는 필수 입력 항목입니다.");
        }

        if (planDTO.getEndDate() == null || planDTO.getEndDate().trim().isEmpty()) {
            throw new BadRequestException("종료 날짜는 필수 입력 항목입니다.");
        }

        // 날짜 형식 및 유효성 검증
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(planDTO.getStartDate(), formatter);
            LocalDate endDate = LocalDate.parse(planDTO.getEndDate(), formatter);

            if (startDate.isAfter(endDate)) {
                throw new BadRequestException("출발 날짜는 종료 날짜보다 이전이어야 합니다.");
            }

            if (startDate.isBefore(LocalDate.now())) {
                throw new BadRequestException("출발 날짜는 오늘 이후여야 합니다.");
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException("날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요.");
        }

        // 예산 검증
        if (planDTO.getBudget() < 0) {
            throw new BadRequestException("예산은 0 이상이어야 합니다.");
        }
    }
}
