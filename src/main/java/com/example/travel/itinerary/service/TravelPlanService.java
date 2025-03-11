package com.example.travel.itinerary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.travel.itinerary.model.Schedule;
import com.example.travel.itinerary.model.TravelPlan;
import com.example.travel.itinerary.repository.ScheduleRepository;
import com.example.travel.itinerary.repository.TravelPlanRepository;

@Service
public class TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private GPTService gptService;

    /**
     * (1) 사용자 입력 + GPT 생성 → TravelPlan 저장
     */
    public TravelPlan createPlan(TravelPlan plan) {
        // GPT로 일정 JSON 생성
        String planContent = gptService.generatePlanContent(plan);
        plan.setItinerary(planContent);

        // TravelPlan 저장
        TravelPlan saved = travelPlanRepository.save(plan);
        return saved;
    }

    /**
     * (2) planId로 TravelPlan 조회
     */
    public TravelPlan getPlan(Long planId) {
        return travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("TravelPlan을 찾을 수 없습니다. planId=" + planId));
    }

    /**
     * (3) 편집 완료 후 최종 확정 → Schedule 생성
     *     - TravelPlan 정보 + 편집된 JSON을 Schedule에 복사
     */
    /**
     * 여행 계획을 확정하고 Schedule 엔티티를 생성합니다.
     * @param planId 여행 계획 ID
     * @param editedItinerary 최종 편집된 일정 JSON
     * @param userId 사용자 ID
     * @param planName 모달에서 입력받은 여행 이름
     * @param planPhoto 모달에서 입력받은 사진 URL
     * @param planDescription 모달에서 입력받은 여행 설명
     * @return 저장된 Schedule 객체
     */
    public Schedule confirmPlan(Long planId, String editedItinerary, String userId, String planName, String planPhoto, String planDescription) {
        TravelPlan travelPlan = getPlan(planId);
        Schedule schedule = createScheduleFromTravelPlan(travelPlan, userId, editedItinerary);
        // 추가 정보를 Schedule에 반영
        schedule.setPlanName(planName);
        schedule.setPlanPhoto(planPhoto);
        schedule.setPlanDescription(planDescription);

        // TravelPlan은 완료 상태로 변경
        travelPlan.setCompleted(true);
        travelPlanRepository.save(travelPlan);

        // Schedule 저장
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return savedSchedule;
    }

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
     * (4) 모달에서 받은 planName, planPhoto, planDescription을 Schedule에 업데이트
     */
    public Schedule updateAdditionalInfo(Long scheduleId, String planName, String planPhoto, String planDescription) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule을 찾을 수 없습니다. id=" + scheduleId));

        schedule.setPlanName(planName);
        schedule.setPlanPhoto(planPhoto);
        schedule.setPlanDescription(planDescription);

        return scheduleRepository.save(schedule);
    }
}
