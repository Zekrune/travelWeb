package com.example.travel.plan.service;

import com.example.travel.plan.model.TravelPlan;
import com.example.travel.plan.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private GPTService gptService;

    public TravelPlan createPlan(TravelPlan plan) {
        // GPT를 이용해 여행 일정 콘텐츠 생성
        String planContent = gptService.generatePlanContent(plan);
        plan.setItinerary(planContent);
        // planId가 없으면 생성
        if (plan.getPlanId() == null || plan.getPlanId().isEmpty()) {
            plan.setPlanId(UUID.randomUUID().toString());
        }
        return travelPlanRepository.save(plan);
    }

    public TravelPlan getPlan(String planId) {
        return travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));
    }
}
