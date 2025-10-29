package com.monew.monew_server.domain.article.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.dto.ArticleResponse;
import com.monew.monew_server.domain.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.mapper.ArticleMapper;
import com.monew.monew_server.domain.article.repository.ArticleRepositoryCustom;
import com.monew.monew_server.domain.article.repository.ArticleViewRepository;
import com.monew.monew_server.domain.comment.repository.CommentRepository;
import com.monew.monew_server.exception.ArticleNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepositoryCustom articleRepository;
	private final ArticleMapper articleMapper;
	private final ArticleViewRepository articleViewRepository;
	private final CommentRepository commentRepository;
	private static final int DEFAULT_PAGE_SIZE = 10;

	public CursorPageResponseArticleDto fetchArticles(ArticleRequest request, UUID currentUserId) {

		// if (request.keyword() != null && request.keyword().isBlank()) {
		// 	throw new IllegalArgumentException("검색 키워드는 비어 있을 수 없습니다.");
		// }

		int requestedSize = request.size() != null ? request.size() : DEFAULT_PAGE_SIZE; // N
		int fetchSize = requestedSize + 1;
		List<Article> fetchedArticles = articleRepository.findArticlesWithFilterAndCursor(request, fetchSize);
		long totalElements = articleRepository.countArticlesWithFilter(request);
		boolean hasNext = fetchedArticles.size() > requestedSize;

		List<ArticleResponse> allResponses = articleMapper.toResponseList(fetchedArticles);

		List<UUID> articleIds = fetchedArticles.stream().map(Article::getId).toList();

		var viewCounts = articleViewRepository.findViewCountsByArticleIds(articleIds)
			.stream().collect(Collectors.toMap(v -> v.getArticleId(), v -> v.getViewCount()));

		var viewedArticleIds = articleViewRepository.findArticleIdsViewedByUser(articleIds, currentUserId);

		var commentCounts = commentRepository.findCommentCountsByArticleIds(articleIds)
			.stream().collect(Collectors.toMap(c -> c.getArticleId(), c -> c.getCommentCount()));

		List<ArticleResponse> enrichedResponses = allResponses.stream().map(resp ->
			new ArticleResponse(
				resp.id(),
				resp.title(),
				resp.summary(),
				resp.sourceUrl(),
				resp.publishDate(),
				commentCounts.getOrDefault(resp.id(), resp.commentCount() != null ? resp.commentCount() : 0L),
				viewCounts.getOrDefault(resp.id(), resp.viewCount() != null ? resp.viewCount() : 0L),
				viewedArticleIds.contains(resp.id())
			)
		).toList();

		String nextCursor = null;
		String nextAfterString = null;
		ArticleSortType sortBy = request.sortBy() == null ? ArticleSortType.DATE : request.sortBy();

		List<ArticleResponse> finalContentList = enrichedResponses;

		if (hasNext) {
			ArticleResponse nextCursorArticle = enrichedResponses.get(requestedSize);

			nextCursor = nextCursorArticle.id().toString();

			switch (sortBy) {
				case DATE -> nextAfterString = nextCursorArticle.publishDate().toString();
				case COMMENT_COUNT -> nextAfterString = String.valueOf(
					nextCursorArticle.commentCount() != null ? nextCursorArticle.commentCount() : 0
				);
				case VIEW_COUNT -> nextAfterString = String.valueOf(
					nextCursorArticle.viewCount() != null ? nextCursorArticle.viewCount() : 0
				);
			}

			finalContentList = enrichedResponses.subList(0, requestedSize);
		} else if (!enrichedResponses.isEmpty() && (sortBy == ArticleSortType.VIEW_COUNT
			|| sortBy == ArticleSortType.COMMENT_COUNT)) {
			ArticleResponse lastArticle = enrichedResponses.get(enrichedResponses.size() - 1);

			if (sortBy == ArticleSortType.VIEW_COUNT) {
				nextAfterString = String.valueOf(lastArticle.viewCount() != null ? lastArticle.viewCount() : 0);
			} else {
				nextAfterString = String.valueOf(lastArticle.commentCount() != null ? lastArticle.commentCount() : 0);
			}
		}

		if (finalContentList.isEmpty() && request.keyword() != null && !request.keyword().isBlank()
			&& (request.cursor() == null || request.cursor().isBlank())) {
			throw new ArticleNotFoundException("검색 결과가 없습니다.");
		}

		return new CursorPageResponseArticleDto(
			finalContentList,
			nextCursor,
			nextAfterString,
			requestedSize,
			hasNext,
			totalElements
		);
	}
}
