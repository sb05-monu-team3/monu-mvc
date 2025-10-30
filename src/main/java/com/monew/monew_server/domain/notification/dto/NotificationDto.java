package com.monew.monew_server.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.monew.monew_server.domain.notification.entity.NotificationResourceType;

public record NotificationDto(
	UUID id,
	boolean confirmed,
	UUID userId,
	String content,
	NotificationResourceType resourceType,
	UUID resourceId,
	Instant createdAt,
	Instant updatedAt
) {
}