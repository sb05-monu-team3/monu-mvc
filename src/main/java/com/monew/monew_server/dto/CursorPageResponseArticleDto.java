package com.monew.monew_server.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponseArticleDto {

	private List<ArticleResponse> content;
	private String nextCursor;
	private String nextAfter;
	private int size;
	private boolean hasNext;
	private long totalElements;
}
