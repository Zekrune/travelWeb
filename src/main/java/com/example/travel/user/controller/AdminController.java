package com.example.travel.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

	@GetMapping("admin/main")
	public String main() {
		return "redirect:/";
	}
}
