package com.monew.monew_server.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.monew.monew_server.domain.notification.dto.CursorPageResponse;
import com.monew.monew_server.domain.notification.dto.NotificationDto;
import com.monew.monew_server.domain.notification.entity.Notification;
import com.monew.monew_server.domain.notification.entity.NotificationResourceType;
import com.monew.monew_server.domain.notification.exception.NotificationNotFoundException;
import com.monew.monew_server.domain.notification.mapper.NotificationMapper;
import com.monew.monew_server.domain.notification.repository.NotificationRepository;
import com.monew.monew_server.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationMapper notificationMapper;

	@InjectMocks
	private NotificationService notificationService;

	private UUID userId;
	private UUID notificationId;
	private Notification notification;
	private NotificationDto notificationDto;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		notificationId = UUID.randomUUID();

		User user = User.builder()
			.id(userId)
			.build();

		notification = Notification.builder()
			.id(notificationId)
			.confirmed(false)
			.user(user)
			.content("Test notification")
			.resourceType(NotificationResourceType.INTEREST)
			.resourceId(UUID.randomUUID())
			.build();

		notificationDto = new NotificationDto(
			notificationId,
			false,
			userId,
			"Test notification",
			NotificationResourceType.INTEREST,
			UUID.randomUUID(),
			Instant.now(),
			Instant.now()
		);
	}

	@Test
	@DisplayName("확인되지 않은 알림 목록 조회 성공")
	void findAllNotConfirmed() {
		// Given
		int limit = 10;
		List<Notification> notifications = List.of(notification);

		given(notificationRepository.findUnconfirmedWithCursor(eq(userId), isNull(), isNull(), eq(limit + 1)))
			.willReturn(notifications);
		given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(1L);

		// When
		CursorPageResponse<NotificationDto> result = notificationService.findAllNotConfirmed(userId, null, null, limit);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.hasNext()).isFalse();
		assertThat(result.totalElements()).isEqualTo(1L);
		verify(notificationRepository).findUnconfirmedWithCursor(userId, null, null, limit + 1);
		verify(notificationRepository).countByUserIdAndConfirmedFalse(userId);
	}

	@Test
	@DisplayName("특정 알림을 확인 성공")
	void confirm() {
		// Given
		given(notificationRepository.findByIdAndUserId(notificationId, userId)).willReturn(Optional.of(notification));

		// When
		notificationService.confirm(notificationId, userId);

		// Then
		assertThat(notification.isConfirmed()).isTrue();
		verify(notificationRepository).findByIdAndUserId(notificationId, userId);
	}

	@Test
	@DisplayName("존재하지 않는 알림 확인 실패")
	void confirm_NotFound() {
		// Given
		given(notificationRepository.findByIdAndUserId(notificationId, userId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> notificationService.confirm(notificationId, userId))
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage("NOTIFICATION_NOT_FOUND");
		verify(notificationRepository).findByIdAndUserId(notificationId, userId);
	}

	@Test
	@DisplayName("모든 확인되지 않은 알림 일괄 확인 성공")
	void confirmAll() {
		// Given
		given(notificationRepository.confirmByUserId(userId)).willReturn(5);

		// When
		notificationService.confirmAll(userId);

		// Then
		verify(notificationRepository).confirmByUserId(userId);
	}

	@Test
	@DisplayName("0개 일괄 확인 성공")
	void confirm_AllEmpty() {
		// Given
		given(notificationRepository.confirmByUserId(userId)).willReturn(0);

		// When
		notificationService.confirmAll(userId);

		// Then
		verify(notificationRepository).confirmByUserId(userId);
	}

	@Test
	@DisplayName("확인되지 않은 알림 개수 조회 성공")
	void countUnconfirmed() {
		// Given
		given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(3L);

		// When
		long count = notificationService.countUnconfirmed(userId);

		// Then
		assertThat(count).isEqualTo(3L);
		verify(notificationRepository).countByUserIdAndConfirmedFalse(userId);
	}

	@Test
	@DisplayName("확인되지 않은 알림 없는 경우 0 반환")
	void countUnconfirmed_Zero() {
		// Given
		given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(0L);

		// When
		long count = notificationService.countUnconfirmed(userId);

		// Then
		assertThat(count).isZero();
		verify(notificationRepository).countByUserIdAndConfirmedFalse(userId);
	}
}