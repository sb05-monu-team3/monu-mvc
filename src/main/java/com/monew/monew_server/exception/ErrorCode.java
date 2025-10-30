package com.monew.monew_server.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	INTEREST_NAME_DUPLICATION("유사한 이름의 관심사가 이미 존재합니다."),
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
	INVALID_REQUEST("잘못된 요청입니다.");

	private final String message;
}
