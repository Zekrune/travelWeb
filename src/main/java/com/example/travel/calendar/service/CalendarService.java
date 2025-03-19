package com.example.travel.calendar.service;

import com.example.travel.calendar.model.CalendarDTO;
import com.example.travel.schedule.model.Schedule;
import com.example.travel.schedule.repositroy.ScheduleRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // 📌 스케줄 데이터를 CalendarDTO로 변환하여 반환
    public List<CalendarDTO> getAllEvents() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules.stream().map(CalendarDTO::new).collect(Collectors.toList());
    }

    // 📌 일정 제목 수정 기능
    @Transactional
    public Schedule updateSchedule(Long id, String newTitle, String newDescription) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다. ID: " + id));

        System.out.println("🛠 기존 제목: " + schedule.getPlanName() + " | 기존 설명: " + schedule.getPlanDescription());

        schedule.setPlanName(newTitle);
        schedule.setPlanDescription(newDescription); // ✅ 여행 설명 업데이트

        System.out.println("✅ 업데이트 완료 - 제목: " + newTitle + " | 설명: " + newDescription);

        return scheduleRepository.save(schedule);
    }

    // 📌 일정 삭제 기능 추가
    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("일정을 찾을 수 없습니다. ID: " + id);
        }
        scheduleRepository.deleteById(id);
    }
}
