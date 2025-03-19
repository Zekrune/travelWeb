package com.example.travel.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel.user.model.RoleType;
import com.example.travel.user.model.User;
import com.example.travel.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

}
