package com.monew.monew_server.domain.article.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleResponse(
	UUID id, // 커서 기반 페이지네이션을 위한 기사 ID 추가
	String title,
	String summary,
	String sourceUrl,
	Instant publishDate,
	Long commentCount,
	Long viewCount,
	Boolean viewedByMe
) {
}
