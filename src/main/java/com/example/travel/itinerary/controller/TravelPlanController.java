package com.example.travel.itinerary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.travel.itinerary.model.Schedule;
import com.example.travel.itinerary.model.TravelPlan;
import com.example.travel.itinerary.service.TravelPlanService;

@Controller
@RequestMapping("/itineraries")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @GetMapping("/mainPage")
	public String mainPage() {
		return "itineraries/mainPage";
	}

    @GetMapping("/createItinerary")
    public String createPlanForm(Model model) {
        model.addAttribute("travelPlan", new TravelPlan());
        return "itineraries/createItinerary";
    }

    @PostMapping("/createItinerary")
    public String createPlanSubmit(@ModelAttribute TravelPlan travelPlan,
            Model model,
            Authentication authentication) {
        travelPlan.setUserId(authentication.getName());
        TravelPlan savedPlan = travelPlanService.createPlan(travelPlan);

        // GPT가 만들어준 JSON
        model.addAttribute("itineraryJson", savedPlan.getItinerary());
        // TravelPlan ID
        model.addAttribute("planId", savedPlan.getPlanId());
        return "itineraries/editItinerary";
    }

    @PostMapping("/confirm")
    public String confirmPlan(@RequestParam("editedItinerary") String editedItinerary,
            @RequestParam("planId") Long planId,
            @RequestParam("planName") String planName,
            @RequestParam("planPhoto") String planPhoto,
            @RequestParam("planDescription") String planDescription,
            Authentication authentication,
            Model model) {
        String userId = authentication.getName();
        Schedule schedule = travelPlanService.confirmPlan(planId, editedItinerary, userId, planName, planPhoto, planDescription);
        model.addAttribute("scheduleItem", schedule);
        return "itineraries/resultItinerary";
    }

    @PostMapping("/updateAdditionalInfo")
    public String updateAdditionalInfo(@RequestParam("scheduleItemId") Long scheduleId,
            @RequestParam("planName") String planName,
            @RequestParam("planPhoto") String planPhoto,
            @RequestParam("planDescription") String planDescription,
            Model model) {
        Schedule updated = travelPlanService.updateAdditionalInfo(scheduleId, planName, planPhoto, planDescription);
        model.addAttribute("scheduleItem", updated);
        return "itineraries/resultItinerary";
    }
}
