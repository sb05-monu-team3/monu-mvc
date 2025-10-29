package com.monew.monew_server.domain.article.repository;

import java.util.List;

import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.entity.Article;

public interface ArticleRepositoryCustom {

	List<Article> findArticlesWithFilterAndCursor(ArticleRequest request, int size);

	long countArticlesWithFilter(ArticleRequest request);
}
