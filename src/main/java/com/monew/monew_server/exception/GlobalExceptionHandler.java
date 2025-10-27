package com.monew.monew_server.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("예상 못한 예외 발생: {}", exception.getMessage(), exception);

		ErrorResponse errorResponse = new ErrorResponse(exception, 500);
		return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
	}

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(BaseException exception) {
		log.error("개발자 정의 예외 발생: code={}, message={}", exception.getErrorCode(), exception.getMessage());

		HttpStatus httpStatus = determineHttpStatus(exception);
		ErrorResponse errorResponse = new ErrorResponse(exception, httpStatus.value());
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	private HttpStatus determineHttpStatus(BaseException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return switch (errorCode) {
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
			case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
			case ARTICLE_NOT_FOUND -> HttpStatus.NOT_FOUND;
		};
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		log.error("요청 유효성 검사 실패: {}", ex.getMessage());

		Map<String, Object> validationErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError)error).getField();
			String errorMessage = error.getDefaultMessage();
			validationErrors.put(fieldName, errorMessage);
		});

		ErrorResponse response = new ErrorResponse(
			Instant.now(),
			"VALIDATION_ERROR",
			"요청 데이터 유효성 검사에 실패했습니다",
			validationErrors,
			ex.getClass().getSimpleName(),
			HttpStatus.BAD_REQUEST.value()
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

}
