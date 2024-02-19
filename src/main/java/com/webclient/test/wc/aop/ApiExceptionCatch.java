package com.webclient.test.wc.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.webclient.test.wc.customannotation.ApiAop;
import com.webclient.test.wc.exception.dto.StatusResponseDto;

import lombok.extern.slf4j.Slf4j;

/***
 * 전역 Exception Handler보다 우선순위를 갖는다.
 */
@Component
@Aspect
@Slf4j
public class ApiExceptionCatch {

	/***
	 * Throws를 통해 지정되지 않은 메소드는 Exception Handler로 넘겨준다.
	 * 
	 * @param joinPoint
	 * @param apiAop
	 * @return
	 * @throws Throwable
	 */
	@Around("@within(apiAop)") // 클래스에 적용된 어노테이션을 찾음
	public Object handlerApiAop(ProceedingJoinPoint joinPoint, ApiAop apiAop) throws Throwable {
		// 클래스 수준의 어노테이션이므로 클래스 정보를 사용하여 메서드 수준의 어노테이션을 가져옴
		String[] arr = apiAop.methods();
//		어노테이션에 무슨 메소드들이 있는지 확인하는 log
//		log.info("arr = {}", Arrays.toString(arr));
		String currentMethod = joinPoint.getSignature().getName();

		if (Arrays.asList(arr).contains(currentMethod)) {
//			log.info("해딩 메소드는 지정되어 있음 :  {}", currentMethod);
			try {
				Object obj = joinPoint.proceed();
				return obj;
			} catch (Throwable e) {
				// 상태코드 + 에러 대표 내용
				log.error("Aop Error = {}", e.getMessage());
				// 상태 코드 + Exception 종류 + 에러 대표 내용
				log.error("Aop toString = {}", e.toString());
				// Exception의 상세 내용
				// log.error("Aop stackTrace = ", e);

				int statusCodeValue = 0;
				String statusText = "";
				HttpStatus httpStatus = null;
				// WebClient에서 넘어온것 Exception
				WebClientResponseException webClientException = (WebClientResponseException) e;
				statusCodeValue = webClientException.getRawStatusCode();
				statusText = webClientException.getStatusText();
				httpStatus = webClientException.getStatusCode();
				log.error("Aop 상태 코드 값 : {}", statusCodeValue);
				log.error("Aop 상태 코드 text : {}", statusText);

				return new ResponseEntity<StatusResponseDto>(
						StatusResponseDto.serverFail(statusCodeValue, e.toString()), httpStatus);
			}
		} else {
			return joinPoint.proceed();
		}
	}
}