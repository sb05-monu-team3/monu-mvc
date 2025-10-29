package com.monew.monew_server.domain.article.repository.projection;

import java.util.UUID;

public interface CommentCountProjection {
	UUID getArticleId();

	Long getCommentCount();
}
