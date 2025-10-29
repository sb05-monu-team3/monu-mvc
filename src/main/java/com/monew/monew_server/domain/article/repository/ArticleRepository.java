package com.monew.monew_server.domain.article.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monew.monew_server.domain.article.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {
	List<Article> findByTitleContaining(String keyword);
}
