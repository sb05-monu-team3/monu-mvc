package com.monew.monew_server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
	INVALID_REQUEST("잘못된 요청입니다."),
	ARTICLE_NOT_FOUND("요청하신 게시글을 찾을 수 없습니다.");
	private final String message;
}
