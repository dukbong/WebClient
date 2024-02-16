package com.webclient.test.wc.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webclient.test.wc.dto.ErrorResponse;
import com.webclient.test.wc.exception.CustomException;
import com.webclient.test.wc.exception.ErrorCode;
import com.webclient.test.wc.exception.dto.StatusResponseDto;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {
	
	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ErrorResponse> customHandler(CustomException e){
		log.error("------------------------------");
		log.error("Custom Handler run...");
		log.error("1. Status : {}", e.getErrorCode().getHttpStatus().value());
		log.error("2. Message : {}", e.getErrorCode().getDetail());
		log.error("------------------------------");
		ErrorCode errorCode = e.getErrorCode();
		return new ResponseEntity<>(ErrorResponse.toErrorResponse(errorCode), errorCode.getHttpStatus());
	}

	// 서버 에러
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<StatusResponseDto> catchHandler(RuntimeException e) {
		log.error("==============================");
		log.error("Catch Handler run...");
		log.error("1. WebClient Exception Message : {}",e.getMessage());
		log.error("2. Line : {}", e.getStackTrace()[0].getLineNumber());
		log.error("==============================");
		return new ResponseEntity<>(StatusResponseDto.serverFail(HttpStatus.GATEWAY_TIMEOUT.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
