package com.example.travel.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	/**
	 * 관리자 대시보드 메인 페이지
	 */
	@GetMapping
	public String adminDashboard(Model model, Authentication authentication) {
		log.info("관리자 대시보드 접속: {}", authentication.getName());

		return "admin/adminMain";
	}

	/**
	 * 관광 명소 관리 페이지
	 */
	@GetMapping("/attractions")
	public String attractions(Model model) {

		return "admin/attractions";
	}
}
