package com.monew.monew_server.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationResourceType {
	INTEREST("interest"),
	COMMENT("comment");

	private final String value;
}
