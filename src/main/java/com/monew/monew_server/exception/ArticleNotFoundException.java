package com.monew.monew_server.exception;

public class ArticleNotFoundException extends BaseException {

	public ArticleNotFoundException(String message) {
		super(ErrorCode.ARTICLE_NOT_FOUND);
	}
}