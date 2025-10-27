package com.monew.monew_server.repository.projection;

import java.util.UUID;

public interface CommentCountProjection {
	UUID getArticleId();

	Long getCommentCount();
}
