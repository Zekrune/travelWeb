package com.example.travel.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel.user.model.RoleType;
import com.example.travel.user.model.User;
import com.example.travel.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 회원가입
	@Transactional
	public User saveUser(User user) {
		// 패스워드 암호화
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		// 권한부여
		user.setRole(RoleType.ROLE_USER);

		return userRepository.save(user);
	}

}
