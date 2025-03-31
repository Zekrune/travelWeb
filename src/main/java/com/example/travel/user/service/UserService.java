package com.example.travel.user.service;

import com.example.travel.exception.ResourceNotFoundException;
import com.example.travel.user.model.RoleType;
import com.example.travel.user.model.User;
import com.example.travel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 회원가입
	@Transactional
	public User saveUser(User user) {
		// 유효성 검사 추가
		if (user.getUsername() == null || user.getPassword() == null) {
			throw new IllegalArgumentException("사용자 이름과 비밀번호는 필수입니다.");
		}
		// 패스워드 암호화
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		// 권한부여
		user.setRole(RoleType.ROLE_USER);
		// 기본 provider 설정 (소셜 로그인이 아닌 경우)
		if (user.getProvider() == null) {
			user.setProvider("local");
		}

		return userRepository.save(user);
	}

	// ✅ 사용자 이름으로 User 정보 가져오기 (로그인한 사용자의 userId 조회)
	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + username));
	}

	// 아이디 중복 여부 확인
	public boolean isUsernameAvailable(String username) {
		log.info("isUsernameAvailable: {}", username);
		return userRepository.findByUsername(username).isEmpty();
	}

	// 닉네임 중복 여부 확인
	public boolean isNicknameAvailable(String nickname) {
		log.info("isNicknameAvailable: {}", nickname);
		return userRepository.findByNickname(nickname).isEmpty();
	}

	// 이메일 중복 여부 확인
	public boolean isEmailAvailable(String email) {
		log.info("isEmailAvailable: {}", email);
		return userRepository.findByEmail(email).isEmpty();
	}

	/**
	 * 인증 객체에서 사용자 정보를 가져오는 메소드
	 * 
	 * @param authentication 인증 객체
	 * @return User 객체
	 */
	public User getUserFromAuthentication(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UsernameNotFoundException("인증 정보가 없습니다.");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
	}

	/**
	 * ID로 유저 조회
	 */
	@Transactional(readOnly = true)
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + id));
	}

	/**
	 * 필터링된 유저 목록 조회
	 */
	@Transactional(readOnly = true)
	public Page<User> getFilteredUsers(String status, String role, String keyword, Pageable pageable) {
		Specification<User> spec = Specification.where(null);

		// 상태 필터 적용
		if (status != null && !status.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
		}

		// 권한 필터 적용
		if (role != null && !role.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
		}

		// 키워드 검색 적용 (이름, 이메일)
		if (keyword != null && !keyword.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.or(
					cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
					cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")));
		}

		return userRepository.findAll(spec, pageable);
	}

	/**
	 * 전체 유저 수 조회
	 */
	@Transactional(readOnly = true)
	public long countAllUsers() {
		return userRepository.count();
	}

	/**
	 * 상태별 유저 수 조회
	 */
	@Transactional(readOnly = true)
	public long countUsersByStatus(String status) {
		return userRepository.countByStatus(status);
	}

	/**
	 * 유저 정보 업데이트
	 */
	@Transactional
	public User updateUser(Long id, User updatedUser) {
		User user = getUserById(id);

		// 업데이트할 필드만 변경
		if (updatedUser.getUsername() != null) {
			user.setUsername(updatedUser.getUsername());
		}

		if (updatedUser.getEmail() != null) {
			user.setEmail(updatedUser.getEmail());
		}

		if (updatedUser.getRole() != null) {
			user.setRole(updatedUser.getRole());
		}

		// 비밀번호 업데이트는 별도 처리 필요 (암호화)

		user.setUpdatedAt(LocalDateTime.now());

		return userRepository.save(user);
	}

	/**
	 * 유저 상태 변경
	 */
	@Transactional
	public User changeUserStatus(Long id, String status) {
		User user = getUserById(id);

		// 상태 검증
		if (!isValidStatus(status)) {
			throw new IllegalArgumentException("유효하지 않은 상태입니다: " + status);
		}

		user.setStatus(status);
		user.setUpdatedAt(LocalDateTime.now());

		if ("suspended".equals(status)) {
			// 정지 시간 설정 (예: 30일)
			user.setSuspendedUntil(LocalDateTime.now().plusDays(30));
		} else {
			user.setSuspendedUntil(null);
		}

		return userRepository.save(user);
	}

	/**
	 * 유저 삭제
	 */
	@Transactional
	public void deleteUser(Long id) {
		User user = getUserById(id);

		// 연관된 데이터 처리 (필요시)

		userRepository.delete(user);
		log.info("사용자 삭제됨: {}", user.getUsername());
	}

	/**
	 * 유저의 여행 일정 수 조회
	 */
	@Transactional(readOnly = true)
	public int countUserItineraries(Long userId) {
		// 실제로는 일정 repository에서 조회 필요
		return 0; // 임시 값
	}

	/**
	 * 유저의 리뷰 수 조회
	 */
	@Transactional(readOnly = true)
	public int countUserReviews(Long userId) {
		// 실제로는 리뷰 repository에서 조회 필요
		return 0; // 임시 값
	}

	/**
	 * 유저의 문의 수 조회
	 */
	@Transactional(readOnly = true)
	public int countUserInquiries(Long userId) {
		// 실제로는 문의 repository에서 조회 필요
		return 0; // 임시 값
	}

	/**
	 * 유저의 로그인 수 조회
	 */
	@Transactional(readOnly = true)
	public int getLoginCount(Long userId) {
		User user = getUserById(userId);
		return user.getLoginCount() != null ? user.getLoginCount() : 0;
	}

	/**
	 * 유저의 최근 활동 조회
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRecentActivities(Long userId) {
		// 실제로는 여러 repository에서 최근 활동을 조회해야 함
		List<Map<String, Object>> activities = new ArrayList<>();

		// 임시 데이터 예시
		Map<String, Object> activity1 = new HashMap<>();
		activity1.put("type", "login");
		activity1.put("timestamp", LocalDateTime.now().minusDays(1));
		activity1.put("description", "시스템 로그인");

		Map<String, Object> activity2 = new HashMap<>();
		activity2.put("type", "review");
		activity2.put("timestamp", LocalDateTime.now().minusDays(3));
		activity2.put("description", "리뷰 작성");

		activities.add(activity1);
		activities.add(activity2);

		return activities;
	}

	/**
	 * 상태값 유효성 검사
	 */
	private boolean isValidStatus(String status) {
		return "active".equals(status) || "inactive".equals(status) || "suspended".equals(status);
	}

}
