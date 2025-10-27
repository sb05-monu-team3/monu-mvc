package com.monew.monew_server.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monew.monew_server.dto.ArticleRequest;
import com.monew.monew_server.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.service.ArticleService;

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
}
