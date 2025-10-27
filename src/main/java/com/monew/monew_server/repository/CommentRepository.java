package com.monew.monew_server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monew.monew_server.domain.comment.entity.Comment;
import com.monew.monew_server.repository.projection.CommentCountProjection;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

	@Query("""
		    SELECT c.article.id AS articleId, COUNT(c) AS commentCount
		    FROM Comment c
		    WHERE c.article.id IN :articleIds AND c.deletedAt IS NULL
		    GROUP BY c.article.id
		""")
	List<CommentCountProjection> findCommentCountsByArticleIds(@Param("articleIds") List<UUID> articleIds);
}
