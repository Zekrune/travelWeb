package com.example.travel.admin.controller;

import com.example.travel.admin.service.AdminService;
import com.example.travel.activity.model.UserActivity;
import com.example.travel.activity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 페이지(대시보드 등) 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;
	private final UserActivityService userActivityService;

	/**
	 * 관리자 대시보드 메인 페이지
	 */
	@GetMapping
	public String adminDashboard(Model model, Authentication authentication) {
		log.info("관리자 대시보드 접속: {}", authentication.getName());

		// 페이징 처리된 최근 활동 조회
		Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
		Page<UserActivity> recentActivities = userActivityService.getRecentActivities(pageable);
		model.addAttribute("recentActivities", recentActivities);

		// 통계 데이터
		model.addAttribute("userCount", adminService.getUserCount());
		model.addAttribute("totalItineraries", adminService.getTotalItinerariesCount());
		model.addAttribute("weeklyCreatedItineraries", adminService.getThisWeekItinerariesCount());
		model.addAttribute("pendingSpotRequests", adminService.getPendingSpotRequestsCount());
		model.addAttribute("unansweredInquiries", adminService.getUnansweredInquiriesCount());
		model.addAttribute("reportedReviews", adminService.getReportedReviewsCount());

		return "admin/adminMain";
	}

	/**
	 * 관광 명소 관리 페이지
	 */
	@GetMapping("/attractions")
	public String attractions(Model model) {
		// 관광 명소 데이터 가져오기
		model.addAttribute("attractions", adminService.getAllAttractions());

		return "admin/attractions";
	}

	/**
	 * 시스템 로그 관리 페이지
	 */
	@GetMapping("/logs")
	public String logs(Model model) {
		// 시스템 로그 데이터 가져오기
		model.addAttribute("logs", adminService.getSystemLogs());

		return "admin/logs";
	}
}
