package com.example.travel.schedule.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.schedule.dto.ScheduleDTO;
import com.example.travel.schedule.service.ScheduleService;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 사용자의 일정 목록 페이지를 표시합니다.
     */
    @GetMapping("/mypage")
    public String userMyPage(Model model, Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByUserId(userId);
            log.info("사용자 {}의 일정 {}개 조회됨", userId, schedules.size());
            model.addAttribute("schedules", schedules);
            return "schedule/myPage";
        } catch (Exception e) {
            log.error("일정 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "일정 목록을 불러오는 중 오류가 발생했습니다.");
            return "schedule/myPage";
        }
    }

    /**
     * 특정 일정의 상세 정보 페이지를 표시합니다.
     */
    @GetMapping("/details/{scheduleId}")
    public String scheduleDetails(@PathVariable("scheduleId") Long scheduleId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            String userId = authentication.getName();
            // 권한 확인이 포함된 일정 조회
            ScheduleDTO schedule = scheduleService.getScheduleWithAuth(scheduleId, userId);
            log.info("일정 ID {} 상세 정보 조회됨", scheduleId);
            model.addAttribute("scheduleItem", schedule);
            return "schedule/myPagedetails";
        } catch (ResourceNotFoundException e) {
            log.warn("일정 상세 조회 중 리소스 없음: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "요청하신 일정을 찾을 수 없습니다.");
            return "redirect:/schedule/myPage";
        } catch (Exception e) {
            log.error("일정 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "일정 정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/schedule/myPage";
        }
    }

    /**
     * 일정을 삭제합니다.
     */
    @PostMapping("/delete/{scheduleId}")
    public String deleteSchedule(@PathVariable("scheduleId") Long scheduleId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String userId = authentication.getName();
            scheduleService.deleteSchedule(scheduleId, userId);
            log.info("일정 ID {} 삭제됨", scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "일정이 성공적으로 삭제되었습니다.");
            return "redirect:/schedule/myPage";
        } catch (ResourceNotFoundException e) {
            log.warn("일정 삭제 중 리소스 없음: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "요청하신 일정을 찾을 수 없습니다.");
            return "redirect:/schedule/myPage";
        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "일정 삭제 중 오류가 발생했습니다.");
            return "redirect:/schedule/myPage";
        }
    }
}
