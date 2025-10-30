package com.monew.monew_server.domain.notification.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponse<T>(
	List<T> content,
	String nextCursor,
	Instant nextAfter,
	int size,
	boolean hasNext,
	Long totalElements
) {
}