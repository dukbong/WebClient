package com.webclient.test.wc.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webclient.test.wc.exception.dto.StatusResponseDto;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {

	// 서버 에러
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<StatusResponseDto> catchHandler(RuntimeException e) {
		log.error("==============================");
		log.error("1. {}",e.getMessage());
		log.error("2. {}",e.getCause());
		log.error("==============================");
		return new ResponseEntity<>(StatusResponseDto.serverFail(HttpStatus.INSUFFICIENT_STORAGE.value(), "test"), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
