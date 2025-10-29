package com.monew.monew_server.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monew.monew_server.domain.user.dto.UserRegisterRequest;
import com.monew.monew_server.domain.user.entity.User;
import com.monew.monew_server.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;

	public void register(UserRegisterRequest request) {
		// 이메일 중복 검사
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}

		// 닉네임 중복 검사
		if (userRepository.existsByNickname(request.getNickname())) {
			throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
		}

		// 암호화 없이 비밀번호 그대로 저장 (임시)
		User user = User.builder()
			.email(request.getEmail())
			.nickname(request.getNickname())
			.password(request.getPassword())
			.build();

		userRepository.save(user);
	}

	public User login ()
}
