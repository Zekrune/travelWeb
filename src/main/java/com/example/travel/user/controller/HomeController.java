package com.example.travel.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import com.example.travel.user.config.AuthenticatedUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HomeController {

	@GetMapping("/main")
	public String main(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
		log.info("authenticatedUser: {}", authenticatedUser);
		model.addAttribute("username", authenticatedUser.getUsername());
		return "main";
	}

	@GetMapping
	public String home(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
		log.info("authenticatedUser: {}", authenticatedUser);
		return "index";
	}
}
