package com.monew.monew_server.domain.article.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monew.monew_server.auth.AuthUser;
import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.dto.ArticleResponse;
import com.monew.monew_server.domain.article.dto.ArticleSourceDto;
import com.monew.monew_server.domain.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.domain.article.service.ArticleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

	private final ArticleService articleService;

	public ArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	@GetMapping
	public ResponseEntity<CursorPageResponseArticleDto> getArticles(
		@Valid @ModelAttribute ArticleRequest request
	) {
		UUID dummyUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
		CursorPageResponseArticleDto articles = articleService.fetchArticles(request, dummyUserId);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/{articleId}")
	public ResponseEntity<ArticleResponse> getArticleById(
		@PathVariable UUID articleId,
		@AuthenticationPrincipal AuthUser authUser
	) {
		UUID userId = authUser != null ? authUser.id() : null;
		ArticleResponse response = articleService.getArticleById(articleId, userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/sources")
	public ResponseEntity<List<ArticleSourceDto>> getSources() {
		return ResponseEntity.ok(articleService.getAllSources());
	}

	@PostMapping("/{articleId}/article-views")
	public ResponseEntity<Void> addArticleView(@PathVariable UUID articleId,
		@AuthenticationPrincipal AuthUser authUser) {
		UUID userId = authUser != null ? authUser.id() : null;
		System.out.println("Current User ID: " + userId);
		articleService.addArticleView(articleId, userId);
		return ResponseEntity.ok().build();
	}
}
