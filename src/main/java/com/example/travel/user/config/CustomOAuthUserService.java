package com.example.travel.user.config;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.travel.user.model.RoleType;
import com.example.travel.user.model.User;
import com.example.travel.user.repository.UserRepository;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
// 소셜 로그인을 성공 했을 때 로그인 정보에 대한 값을 loadUser 메소드에 받는다.
@Service
public class CustomOAuthUserService extends DefaultOAuth2UserService {
	private final UserRepository userRepository;
	@Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("loadUser 실행");
        log.info("userRequest: {}", userRequest);
        log.info("userRequest.getClientRegistration(): {}", userRequest.getClientRegistration());
        log.info("userRequest.getAccessToken(): {}", userRequest.getAccessToken());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User: {}", oAuth2User);
        log.info("oAuth2User.getAttributes(): {}", oAuth2User.getAttributes());

		/*
		 * 소셜 로그인 절차
		 * 1. 서비스에 처음 로그인 하는 사용자
		 * -> 자동으로 회원가입을 시킨다.
		 * username은 이메일을 사용, 패스워드는 임의의 값(1234)으로 넣는다.
		 *
		 * 2. 이전에 로그인을 한적이 있는 사용자
		 * -> 이미 회원 가입이 되어 있기 때문에 회원 DB에서 검색한다.
		 *
		 */
		String email = "";
		String name = "";
		String provider = "";	// 소셜 로그인 서비스
		User user = null;
		Optional<User> findUser = userRepository.findByUsername(email);
		if(findUser.isPresent()) {
			// 이미 회원가입이 되어 있는 사용자
			user = findUser.get();
		} else {
			// 회원 가입 진행
			email = oAuth2User.getAttribute("email");
			name = oAuth2User.getAttribute("name");
			provider = userRequest.getClientRegistration().getRegistrationId();
			user = new User();
			user.setUsername(email);
			user.setPassword("1234");
			user.setName(name);
			user.setEmail(email);
			user.setRole(RoleType.ROLE_USER);
			user.setProvider(provider);

			userRepository.save(user);
		}
		return new AuthenticatedUser(user, oAuth2User.getAttributes());
    }

}