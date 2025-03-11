package com.example.travel.itinerary.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.travel.itinerary.model.Schedule;
import com.example.travel.itinerary.service.ScheduleService;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/myPage")
    public String userMyPage(Model model, Authentication authentication) {
        String userId = authentication.getName();
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(userId);
        log.info("schedules: {}", schedules);
        model.addAttribute("schedules", schedules);
        return "schedule/myPage";
    }

    @GetMapping("/details/{scheduleId}")
    public String scheduleDetails(@PathVariable("scheduleId") Long scheduleId, Authentication authentication,
            Model model) {
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        log.info("schedule: {}", schedule);
        model.addAttribute("scheduleItem", schedule);
        return "schedule/myPagedetails";
    }
}
