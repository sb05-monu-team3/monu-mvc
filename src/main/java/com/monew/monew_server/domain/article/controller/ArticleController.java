package com.monew.monew_server.domain.article.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.dto.ArticleResponse;
import com.monew.monew_server.domain.article.dto.ArticleSourceDto;
import com.monew.monew_server.domain.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.domain.article.service.ArticleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

	private final ArticleService articleService;

	private static final String USER_ID_HEADER = "Monew-Request-User-ID";

	private UUID getUserIdFromHeader(String userIdHeader) {
		if (userIdHeader == null || userIdHeader.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(userIdHeader);
		} catch (IllegalArgumentException e) {
			log.error("{} 잘못된 UUID 형식: {}", USER_ID_HEADER, userIdHeader);
			return null;
		}
	}

	@GetMapping
	public ResponseEntity<CursorPageResponseArticleDto> getArticles(
		@Valid @ModelAttribute ArticleRequest request,
		@RequestHeader(value = USER_ID_HEADER, required = false) String userIdHeader
	) {
		UUID userId = getUserIdFromHeader(userIdHeader);
		log.info("GET /api/articles - (사용자 ID: {})", userId);

		CursorPageResponseArticleDto articles = articleService.fetchArticles(request, userId);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/{articleId}")
	public ResponseEntity<ArticleResponse> getArticleById(
		@PathVariable UUID articleId,
		@RequestHeader(value = USER_ID_HEADER, required = false) String userIdHeader
	) {
		UUID userId = getUserIdFromHeader(userIdHeader);
		log.info("GET /api/articles/{} - (사용자 ID: {})", articleId, userId);

		ArticleResponse response = articleService.getArticleById(articleId, userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/sources")
	public ResponseEntity<List<ArticleSourceDto>> getSources() {
		log.info("GET /api/articles/sources");
		return ResponseEntity.ok(articleService.getAllSources());
	}

	@PostMapping("/{articleId}/article-views")
	public ResponseEntity<Void> addArticleView(@PathVariable UUID articleId,
		@RequestHeader(value = USER_ID_HEADER, required = false) String userIdHeader) {

		UUID userId = getUserIdFromHeader(userIdHeader);
		log.info("POST /api/articles/{}/article-views - (사용자 ID: {})", articleId, userId);

		articleService.addArticleView(articleId, userId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{articleId}")
	public ResponseEntity<Void> softDeleteArticle(@PathVariable UUID articleId) {
		articleService.softDeleteArticle(articleId);
		return ResponseEntity.ok().build();
	}
}
