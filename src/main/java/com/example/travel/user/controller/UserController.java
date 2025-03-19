package com.example.travel.user.controller;

import com.example.travel.user.dto.UserCreateDto;
import com.example.travel.user.model.RoleType;
import com.example.travel.user.model.User;
import com.example.travel.user.service.RecaptchaService;
import com.example.travel.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

	private final UserService userService;
	private final RecaptchaService recaptchaService; // RecaptchaService 주입

	// 회원가입 페이지 이동
	@GetMapping("users/register")
	public String register(Model model) {
		model.addAttribute("userCreateDto", new UserCreateDto());
		return "users/register";
	}

	// 아이디 중복 확인 (AJAX)
	@GetMapping("/api/users/check-username")
	@ResponseBody
	public ResponseEntity<String> checkUsername(@RequestParam String username) {
		// 유효성 검사 - 아이디 패턴 (4-20자 영문, 숫자 조합)
		if (username == null || username.isEmpty()) {
			return ResponseEntity.ok("아이디는 필수 정보입니다.");
		} else if (!username.matches("^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{4,20}$")) {
			return ResponseEntity.ok("아이디는 영문, 숫자 조합 (4-20자)이어야 합니다.");
		} else {
			boolean available = userService.isUsernameAvailable(username);
			String message = available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
			return ResponseEntity.ok(message);
		}
	}

	// 닉네임 중복 확인 (AJAX)
	@GetMapping("/api/users/check-nickname")
	@ResponseBody
	public ResponseEntity<String> checkNickname(@RequestParam String nickname) {
		if (nickname == null || nickname.isEmpty()) {
			return ResponseEntity.ok("닉네임은 필수 정보입니다.");
		} else {
			boolean available = userService.isNicknameAvailable(nickname);
			String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
			return ResponseEntity.ok(message);
		}
	}

	// 이메일 중복 확인 (AJAX)
	@GetMapping("/api/users/check-email")
	@ResponseBody
	public ResponseEntity<String> checkEmail(@RequestParam String email) {
		if (email == null || email.isEmpty()) {
			return ResponseEntity.ok("이메일은 필수 정보입니다.");
		} else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
			return ResponseEntity.ok("올바른 이메일 형식이 아닙니다.");
		} else {
			boolean available = userService.isEmailAvailable(email);
			String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
			return ResponseEntity.ok(message);
		}
	}

	// 회원가입 (API 버전 - AJAX용)
	@PostMapping("/api/users/register")
	@ResponseBody
	public ResponseEntity<?> registerUserApi(@Valid @RequestBody UserCreateDto userCreateDto,
			BindingResult bindingResult) {
		log.info("API 회원가입 요청: {}", userCreateDto);

		// 비밀번호 확인 검사
		if (!userCreateDto.getPassword().equals(userCreateDto.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "error.userCreateDto", "비밀번호가 일치하지 않습니다.");
		}

		// reCAPTCHA 검증
		if (!recaptchaService.verify(userCreateDto.getRecaptchaResponse())) {
			bindingResult.rejectValue("recaptchaResponse", "error.userCreateDto", "reCAPTCHA 검증에 실패했습니다.");
		}

		// 유효성 검사 실패 시 에러 메시지 반환
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			bindingResult.getFieldErrors().forEach(error -> {
				errors.put(error.getField(), error.getDefaultMessage());
			});
			log.warn("회원가입 유효성 검사 실패: {}", errors);
			return ResponseEntity.badRequest().body(errors);
		}

		// DTO -> Entity 변환
		User user = new User();
		user.setUsername(userCreateDto.getUsername());
		user.setPassword(userCreateDto.getPassword());
		user.setName(userCreateDto.getName());
		user.setNickname(userCreateDto.getNickname());
		user.setGender(userCreateDto.getGender());
		user.setBirthdate(userCreateDto.getBirthdate());
		user.setEmail(userCreateDto.getEmail());
		user.setProvider("local");
		user.setRole(RoleType.ROLE_USER);

		// 회원가입 처리
		userService.saveUser(user);
		log.info("회원가입 성공: {}", user.getUsername());

		Map<String, String> response = new HashMap<>();
		response.put("message", "회원가입이 완료되었습니다.");
		return ResponseEntity.ok(response);
	}

	// 회원가입 (기존 form submit 버전)
	@PostMapping("users/register")
	public String registerUser(@Valid @ModelAttribute("userCreateDto") UserCreateDto userCreateDto,
			BindingResult bindingResult, Model model) {
		log.info("일반 폼 회원가입 요청: {}", userCreateDto);

		// 비밀번호 확인 검사
		if (!userCreateDto.getPassword().equals(userCreateDto.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "error.userCreateDto", "비밀번호가 일치하지 않습니다.");
		}

		// reCAPTCHA 검증
		if (!recaptchaService.verify(userCreateDto.getRecaptchaResponse())) {
			bindingResult.rejectValue("recaptchaResponse", "error.userCreateDto", "reCAPTCHA 검증에 실패했습니다.");
		}

		// 필드 검증 실패 시 다시 회원가입 페이지로
		if (bindingResult.hasErrors()) {
			log.warn("회원가입 유효성 검사 실패 (폼 제출)");
			return "users/register";
		}

		// DTO -> Entity 변환
		User user = new User();
		user.setUsername(userCreateDto.getUsername());
		user.setPassword(userCreateDto.getPassword()); // 패스워드 암호화는 service에서 처리
		user.setName(userCreateDto.getName());
		user.setNickname(userCreateDto.getNickname());
		user.setGender(userCreateDto.getGender());
		user.setBirthdate(userCreateDto.getBirthdate());
		user.setEmail(userCreateDto.getEmail());
		user.setProvider("local"); // 자동 설정
		user.setRole(RoleType.ROLE_USER); // 자동 설정

		// 회원가입 처리
		userService.saveUser(user);
		log.info("회원가입 성공 (폼 제출): {}", user.getUsername());

		return "redirect:/users/login";
	}

	// 로그인 페이지 이동
	@GetMapping("users/login")
	public String login(
			@RequestParam(name = "loginError", required = false) boolean loginError,
			@RequestParam(name = "errorMessage", required = false) String errorMessage,
			Model model) {
		log.info("로그인 페이지 접근, loginError: {}, errorMessage: {}", loginError, errorMessage);
		if (errorMessage != null)
			model.addAttribute("errorMessage", errorMessage);
		model.addAttribute("user", new User());
		return "users/login";
	}

	@GetMapping(value = "users/login-success-json", produces = "application/json")
	@ResponseBody
	public Map<String, Object> loginSuccessJson(HttpSession session) {
		log.info("✅ 로그인 성공 (JSON 응답)");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("인증되지 않은 사용자입니다.");
		}

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		User user = userService.findByUsername(userDetails.getUsername());
		if (user == null) {
			throw new IllegalStateException("사용자를 찾을 수 없습니다: " + userDetails.getUsername());
		}

		session.setAttribute("userId", user.getId());

		Map<String, Object> response = new HashMap<>();
		response.put("userId", user.getId());
		response.put("username", user.getUsername());
		response.put("message", "로그인 성공");

		return response;
	}

	@GetMapping("users/login-success")
	public String loginSuccess(HttpSession session) {
		log.info("✅ 로그인 성공!");
		return "redirect:/main";
	}

	@GetMapping("users/login-fail")
	public String loginFail() {
		log.info("❌ 로그인 실패!");
		return "redirect:/users/login";
	}
}