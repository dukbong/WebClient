package com.webclient.test.wc.exception.advice;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {
	
//	@ExceptionHandler(value = CustomException.class)
//	public ResponseEntity<ErrorResponse> customHandler(CustomException e){
////	public void customHandler(CustomException e){
//		log.error("------------------------------");
//		log.error("Custom Handler run...");
//		log.error("1. Status : {}", e.getErrorCode().getHttpStatus().value());
//		log.error("2. Message : {}", e.getErrorCode().getDetail());
//		log.error("------------------------------");
//		ErrorCode errorCode = e.getErrorCode();
//		return new ResponseEntity<>(ErrorResponse.toErrorResponse(errorCode), errorCode.getHttpStatus());
//	}

	// httpClient에서 지정한 Exception 처리를 위한 핸들러
	// 이게 없다면 RuntimeException을 처리하는 곳에서 받는다.
//	@ExceptionHandler(value = WebClientRequestException.class)
//	public ResponseEntity<StatusResponseDto> readTimeOut(WebClientRequestException e){
//	public void readTimeOut(WebClientRequestException e){
//	    log.error("ReadTimeoutException occurred: {}", e.getMessage());
//	    return new ResponseEntity<>(StatusResponseDto.serverFail(HttpStatus.REQUEST_TIMEOUT.value(), e.getMessage()), HttpStatus.REQUEST_TIMEOUT);
//	}
	
	// 서버 에러
	@ExceptionHandler({Exception.class, RuntimeException.class})
//	public ResponseEntity<StatusResponseDto> catchHandler(RuntimeException e) {
	public void catchHandler(RuntimeException e) throws Exception {
		log.error("==============================");
		log.error("Catch Handler run...");
		log.error("1. Exception Message : {}",e.getMessage());
		log.error("2. Line : {}", e.getStackTrace()[0].getLineNumber());
		log.error("요청 서버의 Exception Handler에게 전달");
		log.error("==============================");
//		return new ResponseEntity<>(StatusResponseDto.serverFail(ErrorCode.TEST_500.getHttpStatus().value(), ErrorCode.TEST_500.getDetail()), HttpStatus.INTERNAL_SERVER_ERROR);
//		return new ResponseEntity<>(StatusResponseDto.serverFail(, ErrorCode.TEST_500.getDetail()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
