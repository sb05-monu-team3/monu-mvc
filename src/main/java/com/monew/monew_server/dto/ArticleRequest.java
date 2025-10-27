package com.monew.monew_server.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.entity.ArticleSource;

public record ArticleRequest(
	String keyword,             // 제목 또는 요약 검색어
	List<UUID> interestIds,     // 관심사 ID 목록
	ArticleSource source,       // 출처 필터링
	LocalDate date,             // 날짜 필터링
	String cursor,              // 커서 기반 페이지네이션을 위한 다음 커서 값
	ArticleSortType sortBy,     // 정렬 기준 (날짜, 댓글 수, 조회 수)
	Integer size,                // 페이지 크기
	String nextAfter
) {
}
