package com.example.travel.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel.user.model.User;
import com.example.travel.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

	private final UserService userService;

	// 회원가입 페이지 이동
	@GetMapping("users/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "users/register";
	}

	// 회원가입
	@PostMapping("users/register")
	public String registerUser(@ModelAttribute User user) {
		log.info("user: {}", user);
		userService.saveUser(user);
		return "redirect:/users/login";
	}

	// 로그인 페이지 이동
	@GetMapping("users/login")
	public String login(
			@RequestParam(name = "loginError", required = false) boolean loginError,
			@RequestParam(name = "errorMessage", required = false) String errorMessage,
			Model model) {
		log.info("loginError: {}", loginError);
		log.info("errorMessage: {}", errorMessage);
		if (errorMessage != null)
			model.addAttribute("errorMessage", errorMessage);
		model.addAttribute("user", new User());
		return "users/login";
	}

	// 로그인 성공 시 이동 URL
	@GetMapping("users/login-success")
	public String loginSuccess() {
		log.info("로그인 성공!");
		return "redirect:/main";
	}

	// 로그인 실패 시 이동 URL
	@GetMapping("users/login-fail")
	public String loginFail() {
		log.info("로그인 실패!");
		return "redirect:/users/login";
	}

}
