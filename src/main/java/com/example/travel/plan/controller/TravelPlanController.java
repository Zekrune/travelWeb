package com.example.travel.plan.controller;

import com.example.travel.plan.model.TravelPlan;
import com.example.travel.plan.service.GPTService;
import com.example.travel.plan.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/plans")
public class TravelPlanController {

    @Autowired
    private GPTService gptService;

    @Autowired
    private TravelPlanService travelPlanService;

    @GetMapping("/planMain")
    public String planMain() {
        return "plans/PlanMain";
    }

    // 여행 일정 생성 폼 표시
    @GetMapping("/create")
    public String showCreatePlanForm(Model model) {
        model.addAttribute("travelPlan", new TravelPlan());
        return "plans/createPlan";
    }

    // 폼 제출 후 여행 일정 생성
    @PostMapping("/create")
    public String createPlan(@ModelAttribute TravelPlan travelPlan, Model model) {
        try {
            TravelPlan createdPlan = travelPlanService.createPlan(travelPlan);
            model.addAttribute("travelPlan", createdPlan);
            return "plans/planResult";
        } catch(Exception e) {
            model.addAttribute("error", e.getMessage());
            return "plans/createPlan";
        }
    }

    // 특정 planId의 여행 일정 조회
    @GetMapping("/{planId}")
    public String viewPlan(@PathVariable String planId, Model model) {
        TravelPlan plan = travelPlanService.getPlan(planId);
        model.addAttribute("travelPlan", plan);
        return "plans/planResult";
    }
}
