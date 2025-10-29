package com.monew.monew_server.domain.notification.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monew.monew_server.domain.notification.dto.CursorPageResponse;
import com.monew.monew_server.domain.notification.dto.NotificationDto;
import com.monew.monew_server.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<CursorPageResponse<NotificationDto>> findAllNotConfirmed(
		@RequestHeader("Monew-Request-User-ID") UUID userId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
		@RequestParam int limit) {

		log.info("알림 목록 조회 시작: userId={}, cursor={}, after={}, limit={}",
			userId, cursor, after, limit);

		CursorPageResponse<NotificationDto> response =
			notificationService.findAllNotConfirmed(userId, cursor, after, limit);

		log.debug("알림 목록 조회 완료: size={}, hasNext={}", response.size(), response.hasNext());

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{notificationId}")
	public ResponseEntity<Void> confirm(
		@RequestHeader("Monew-Request-User-ID") UUID userId,
		@PathVariable UUID notificationId) {

		log.info("특정 알림 확인 시작: notificationId={}, userId={}", notificationId, userId);

		notificationService.confirm(notificationId, userId);

		return ResponseEntity.ok().build();
	}

	@PatchMapping
	public ResponseEntity<Void> confirmAll(
		@RequestHeader("Monew-Request-User-ID") UUID userId) {

		log.info("전체 알림 확인 시작: userId={}", userId);

		notificationService.confirmAll(userId);

		return ResponseEntity.ok().build();
	}
}