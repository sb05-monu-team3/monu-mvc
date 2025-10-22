package com.monew.monew_server.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationResourceType {
	INTEREST("interest"),
	COMMENT("comment");

	private final String value;
}
