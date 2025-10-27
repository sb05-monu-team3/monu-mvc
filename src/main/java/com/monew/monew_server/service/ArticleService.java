package com.monew.monew_server.service;

// @Service
// @RequiredArgsConstructor
// public class ArticleService {
//
// 	private final ArticleRepositoryCustom articleRepository;
// 	private final ArticleMapper articleMapper;
// 	private final int DEFAULT_PAGE_SIZE = 10;
//
// 	public CursorPageResponseArticleDto fetchArticles(ArticleRequest request) {
//
// 		if (request.keyword() != null && request.keyword().isBlank()) {
// 			throw new IllegalArgumentException("검색 키워드는 비어 있을 수 없습니다.");
// 		}
//
// 		int requestedSize = request.size() != null ? request.size() : DEFAULT_PAGE_SIZE;
// 		int fetchSize = requestedSize + 1;
//
// 		List<Article> fetchedArticles = articleRepository.findArticlesWithFilterAndCursor(request, fetchSize);
//
// 		long totalElements = articleRepository.countArticlesWithFilter(request);
//
// 		boolean hasNext = fetchedArticles.size() > requestedSize;
//
// 		List<Article> contentList = hasNext ?
// 			fetchedArticles.subList(0, requestedSize) :
// 			fetchedArticles;
//
// 		List<ArticleResponse> contentResponses = articleMapper.toResponseList(contentList);
//
// 		String nextCursor = null;
// 		String nextAfterString = null;
// 		ArticleSortType sortBy = request.sortBy() == null ? ArticleSortType.DATE : request.sortBy();
//
// 		if (hasNext) {
// 			ArticleResponse lastArticleResponse = contentResponses.get(contentResponses.size() - 1);
//
// 			nextCursor = lastArticleResponse.id().toString();
//
// 			if (sortBy == ArticleSortType.DATE) {
// 				nextAfterString = lastArticleResponse.publishDate().toString();
// 			} else if (sortBy == ArticleSortType.COMMENT_COUNT) {
// 				nextAfterString = String.valueOf(lastArticleResponse.commentCount());
// 			} else if (sortBy == ArticleSortType.VIEW_COUNT) {
// 				nextAfterString = String.valueOf(lastArticleResponse.viewCount());
// 			}
// 		}
//
// 		if (contentResponses.isEmpty() && request.keyword() != null && !request.keyword().isBlank()
// 			&& (request.cursor() == null || request.cursor().isBlank())) {
// 			throw new ArticleNotFoundException("키워드 '" + request.keyword() + "'와(과) 일치하는 기사가 없습니다.");
// 		}
//
// 		return new CursorPageResponseArticleDto(
// 			contentResponses,
// 			nextCursor,
// 			nextAfterString,
// 			requestedSize,
// 			hasNext,
// 			totalElements
// 		);
// 	}
// }

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.dto.ArticleRequest;
import com.monew.monew_server.dto.ArticleResponse;
import com.monew.monew_server.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.mapper.ArticleMapper;
import com.monew.monew_server.repository.ArticleRepositoryCustom;
import com.monew.monew_server.repository.ArticleViewRepository;
import com.monew.monew_server.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepositoryCustom articleRepository;
	private final ArticleMapper articleMapper;
	private final ArticleViewRepository articleViewRepository;
	private final CommentRepository commentRepository; // CommentRepository 추가 필요
	private static final int DEFAULT_PAGE_SIZE = 10;

	public CursorPageResponseArticleDto fetchArticles(ArticleRequest request, UUID currentUserId) {

		if (request.keyword() != null && request.keyword().isBlank()) {
			throw new IllegalArgumentException("검색 키워드는 비어 있을 수 없습니다.");
		}

		int requestedSize = request.size() != null ? request.size() : DEFAULT_PAGE_SIZE;
		int fetchSize = requestedSize + 1;

		List<Article> fetchedArticles = articleRepository.findArticlesWithFilterAndCursor(request, fetchSize);
		long totalElements = articleRepository.countArticlesWithFilter(request);

		boolean hasNext = fetchedArticles.size() > requestedSize;
		List<Article> contentList = hasNext
			? fetchedArticles.subList(0, requestedSize)
			: fetchedArticles;

		List<ArticleResponse> contentResponses = articleMapper.toResponseList(contentList);

		List<UUID> articleIds = contentList.stream().map(Article::getId).toList();

		var viewCounts = articleViewRepository.findViewCountsByArticleIds(articleIds)
			.stream().collect(Collectors.toMap(v -> v.getArticleId(), v -> v.getViewCount()));

		var viewedArticleIds = articleViewRepository.findArticleIdsViewedByUser(articleIds, currentUserId);

		var commentCounts = commentRepository.findCommentCountsByArticleIds(articleIds)
			.stream().collect(Collectors.toMap(c -> c.getArticleId(), c -> c.getCommentCount()));

		contentResponses = contentResponses.stream().map(resp ->
			new ArticleResponse(
				resp.id(),
				resp.title(),
				resp.summary(),
				resp.sourceUrl(),
				resp.publishDate(),
				commentCounts.getOrDefault(resp.id(), 0L),
				viewCounts.getOrDefault(resp.id(), 0L),
				viewedArticleIds.contains(resp.id())
			)
		).toList();

		String nextCursor = null;
		String nextAfterString = null;
		ArticleSortType sortBy = request.sortBy() == null ? ArticleSortType.DATE : request.sortBy();

		if (hasNext) {
			ArticleResponse lastArticleResponse = contentResponses.get(contentResponses.size() - 1);
			nextCursor = lastArticleResponse.id().toString();

			switch (sortBy) {
				case DATE -> nextAfterString = lastArticleResponse.publishDate().toString();
				case COMMENT_COUNT -> nextAfterString = String.valueOf(
					lastArticleResponse.commentCount() != null ? lastArticleResponse.commentCount() : 0
				);
				case VIEW_COUNT -> nextAfterString = String.valueOf(
					lastArticleResponse.viewCount() != null ? lastArticleResponse.viewCount() : 0
				);
			}
		}

		return new CursorPageResponseArticleDto(
			contentResponses,
			nextCursor,
			nextAfterString,
			requestedSize,
			hasNext,
			totalElements
		);
	}
}

