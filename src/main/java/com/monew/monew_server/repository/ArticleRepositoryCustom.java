package com.monew.monew_server.repository;

import java.util.List;

import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.dto.ArticleRequest;

public interface ArticleRepositoryCustom {

	List<Article> findArticlesWithFilterAndCursor(ArticleRequest request, int size);

	long countArticlesWithFilter(ArticleRequest request);
}
