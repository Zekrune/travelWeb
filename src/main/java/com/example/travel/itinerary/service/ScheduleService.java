package com.example.travel.itinerary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.travel.itinerary.model.Schedule;
import com.example.travel.itinerary.repository.ScheduleRepository;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public List<Schedule> getSchedulesByUserId(String userId) {
        return scheduleRepository.findByUserId(userId);
    }

    public Schedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule을 찾을 수 없습니다. id=" + scheduleId));
    }
}
