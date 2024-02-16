package com.webclient.test.wc.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	TEST_404(HttpStatus.NOT_FOUND, "This is not found Exception 404"),
	TEST_500(HttpStatus.INTERNAL_SERVER_ERROR, "This is internal server error 500"),
	TEST_400(HttpStatus.BAD_REQUEST, "This is Bad Request error 400");
	
	private final HttpStatus httpStatus;
	private final String detail;
}
