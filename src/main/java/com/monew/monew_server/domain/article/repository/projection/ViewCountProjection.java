package com.monew.monew_server.domain.article.repository.projection;

import java.util.UUID;

public interface ViewCountProjection {
	UUID getArticleId();

	long getViewCount();
}