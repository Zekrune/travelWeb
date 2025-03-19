package com.example.travel.calendar.controller;

import com.example.travel.calendar.model.CalendarDTO;
import com.example.travel.calendar.service.CalendarService;
import com.example.travel.schedule.model.Schedule;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*") // CORS 문제 해결
public class CalendarController {

    private final CalendarService eventService;

    public CalendarController(CalendarService eventService) {
        this.eventService = eventService;
    }

    // 📌 캘린더에서 일정 조회 API
    @GetMapping
    public ResponseEntity<List<CalendarDTO>> getEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // 📌 일정 제목 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Map<String, String> updateData) {
        String newPlanName = updateData.get("planName");
        String newPlanDescription = updateData.get("planDescription");

        System.out.println("🔍 업데이트 요청 - ID: " + id);
        System.out.println("📝 받은 데이터 - 새로운 제목: " + newPlanName + ", 새로운 설명: " + newPlanDescription);

        if (newPlanName == null || newPlanName.isEmpty()) {
            System.out.println("❌ 오류: 제목이 없습니다.");
            return ResponseEntity.badRequest().body("제목이 비어 있습니다.");
        }
        if (newPlanDescription == null) {
            System.out.println("❌ 오류: 설명이 없습니다.");
            return ResponseEntity.badRequest().body("설명이 비어 있습니다.");
        }

        Schedule updatedSchedule = eventService.updateSchedule(id, newPlanName, newPlanDescription);
        return ResponseEntity.ok(updatedSchedule);
    }

    // 📌 일정 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteSchedule(id);
        return ResponseEntity.ok("일정이 삭제되었습니다.");
    }

    // ✅ 정상적으로 동작하도록 GET 요청 허용
    @GetMapping("/debug")
    public ResponseEntity<List<CalendarDTO>> debugEvents() {
        List<CalendarDTO> events = eventService.getAllEvents();

        // ✅ 디버깅 로그 추가
        events.forEach(event -> System.out.println("🛠 이벤트 JSON 응답: " + event));

        return ResponseEntity.ok(events);
    }
}
