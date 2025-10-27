package com.monew.monew_server.repository.projection;

import java.util.UUID;

public interface ViewCountProjection {
	UUID getArticleId();

	long getViewCount();
}