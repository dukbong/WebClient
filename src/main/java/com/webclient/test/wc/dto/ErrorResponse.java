package com.webclient.test.wc.dto;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.webclient.test.wc.exception.ErrorCode;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ErrorResponse {
	private final Integer status;
	private final String message;
	private final List<String> errors;

	public ErrorResponse(HttpStatus status, String message, String error) {
		this.status = status.value();
		this.message = message;
		this.errors = List.of(error);
	}

	public ErrorResponse(HttpStatus status, String message, List<String> errors) {
		this.status = status.value();
		this.message = message;
		this.errors = errors;
	}

	public static ErrorResponse toErrorResponse(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getHttpStatus(), errorCode.name(), errorCode.getDetail());
	}
}