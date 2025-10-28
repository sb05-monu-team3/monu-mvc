package com.monew.monew_server.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.entity.ArticleSource;

public record ArticleRequest(
	String keyword,
	List<UUID> interestIds,
	ArticleSource source,
	LocalDate date,
	String cursor,
	ArticleSortType sortBy,
	Integer size,
	String nextAfter
) {
}
