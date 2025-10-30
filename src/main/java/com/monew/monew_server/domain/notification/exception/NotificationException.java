package com.monew.monew_server.domain.notification.exception;

import com.monew.monew_server.exception.BaseException;
import com.monew.monew_server.exception.ErrorCode;

public class NotificationException extends BaseException {
	public NotificationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotificationException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
} 