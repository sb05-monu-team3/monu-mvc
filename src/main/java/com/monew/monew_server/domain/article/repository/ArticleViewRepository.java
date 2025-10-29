package com.monew.monew_server.domain.article.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monew.monew_server.domain.article.entity.ArticleView;
import com.monew.monew_server.domain.article.repository.projection.ViewCountProjection;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

	@Query("SELECT COUNT(av) FROM ArticleView av WHERE av.article.id = :articleId")
	long countByArticleId(@Param("articleId") UUID articleId);

	@Query("""
		    SELECT av.article.id AS articleId, COUNT(av) AS viewCount
		    FROM ArticleView av
		    WHERE av.article.id IN :articleIds
		    GROUP BY av.article.id
		""")
	List<ViewCountProjection> findViewCountsByArticleIds(@Param("articleIds") List<UUID> articleIds);

	boolean existsByArticle_IdAndUser_Id(UUID articleId, UUID userId);

	@Query("""
		    SELECT av.article.id
		    FROM ArticleView av
		    WHERE av.user.id = :userId
		    AND av.article.id IN :articleIds
		""")
	Set<UUID> findArticleIdsViewedByUser(@Param("articleIds") List<UUID> articleIds,
		@Param("userId") UUID userId);
}