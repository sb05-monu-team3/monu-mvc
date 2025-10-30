package com.monew.monew_server.domain.notification.exception;

import java.util.UUID;

import com.monew.monew_server.exception.ErrorCode;

public class NotificationNotFoundException extends NotificationException {
	public NotificationNotFoundException() {
		// NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.");
		super(ErrorCode.INVALID_REQUEST);
		// ErrorCode - super(ErrorCode.NOTIFICATION_NOT_FOUND);
		// GlobalExceptionHandler - case NOTIFICATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
	}

	public static NotificationNotFoundException withId(UUID notificationId) {
		NotificationNotFoundException exception = new NotificationNotFoundException();
		exception.addDetail("notificationId", notificationId);
		return exception;
	}
} 