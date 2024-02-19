package com.webclient.test.wc.apicomponent;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


/***
 * 해결해야 하는 사항
 * 1. 어떤 로그를 남길것인가?
 *    api 호출 시 이력을 남김
 *       성공 : doOnSuccess 안에서 로그 기록
 *       실패 : doBeforeRetry 안에서 로그 기록
 *          > doBeforeRetry : 여기서 남길 경우 첫 통신 외 재시도한 경우까지 기록
 *             1. 실제 응답받은 에러는 해당 메소드에서 확인된다.
 *             2. 시간 초과 관련 WebClient에서 설정한 오류도 여기서 확인이 가능하다.
 *          > doOnError : 재시도가 종료된 후 관련 WebClient 입장에서 발생하는 에러를 알 수 있다. (ex : Retries exhausted: 3/3)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiWebClient {
	
	private final WebClient localWebClient;
	private String baseUrl;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public <T> Mono<T> postApi(String uri, Object vo, String token, Class<T> bodyType) {
		
		AtomicInteger retryCount = new AtomicInteger(0);
		
		return localWebClient.post().uri(uri).header("Authorization", "Bearer " + token).body(BodyInserters.fromValue(vo)).accept(MediaType.APPLICATION_JSON)
				.retrieve()
				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is4xxClientError, res -> {
					log.error("응답 받은 상태 코드 4xx : {}", res.statusCode());
					// 응답 헤더 값 확인 (JWT TOKEN)
					log.error("header = {}", res.headers().asHttpHeaders());
					return res.createException().flatMap(Mono::error);
				})
				
				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is5xxServerError, res -> {
//					log.error("응답 받은 상태 코드 5xx : {}", res.statusCode());
//					log.error("5xx 상태 코드 : {}", res.rawStatusCode());
//					log.error("5xx 요청한 URL + URI : {}", baseUrl + uri);
					// 응답 헤더 값 확인 (JWT TOKEN)
//					log.error("5xx header = {}", res.headers().asHttpHeaders());
					return res.createException().flatMap(Mono::error);
				})
				
				// [body] 와 [bodyType]을 매칭시켜서 가져오겠다.
				// WebClient 로직의 핵심 부분이며 해당 부분은 default로 비동기적으로 실행된다.
				.bodyToMono(bodyType)
				
				// 재시도 관련 내용
				.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
						// [Filter] 5xx 서버 관련 에러 발생 시 재시도 안함
						// :: 서버 에러가 날 경우 서버에서 조치하기 전엔 재시도 할 필요가 없기 때문이다.
						.filter(error -> {
						    // 5xx 서버 에러인지 확인
						    boolean is5xxServerError = error instanceof WebClientResponseException
						            && ((WebClientResponseException) error).getStatusCode().is5xxServerError();
						    
						    // ReadTimeoutException 인지 확인
						    boolean isReadTimeoutException = error instanceof WebClientRequestException
						            && error.getMessage().contains("ReadTimeoutException");

						    // 5xx 서버 에러이거나 ReadTimeoutException이 아닌 경우에만 true를 반환
						    return !is5xxServerError && !isReadTimeoutException;
						})
						// 재시도 하기 전 현재 시도에 대한 정보가 담겨 있다.
						.doBeforeRetry(before -> {
//							log.error("요청 시간 : {}", LocalTime.now());
//							log.error("요청 URL : {}", baseUrl + uri);
							WebClientResponseException webClientResponseException = null;
							if (before != null && before.failure() instanceof WebClientResponseException) {
								webClientResponseException = (WebClientResponseException) before.failure();
//								log.error("상태 코드 값  : {}", webClientResponseException.getRawStatusCode());
//								log.error("에러 내용     : {}", before.failure().toString());
//								log.error("요청 시도 횟수 : {}", (before.totalRetries() + 1));
//								log.error("에러 내용     : {}", before.failure().toString());
							}
							if(webClientResponseException != null) {
//								log.error("상태 코드 값  : {}", webClientResponseException.getStatusCode().value());
							}
							// [재시도 관련 모든 내용을 포함]
							// log.error("시간 : {}, 재시도 내용 : {}", LocalTime.now(), before.toString());
							
							// [오류의 상세 내용]
							// log.error("에러 stack Trace", before.failure());
							
							// [성공시 몇번만에 성공했는지 알고 싶다면 사용 가능]
							retryCount.incrementAndGet();
						}))
				// 재시도 종료 후 에러가 발생하면 확인할 수 있다.
				.doOnError(error -> {
					log.error("1. 재시도 종료  : {}", error.getMessage());
				})
				.doOnSuccess(success -> {
//					여기서 success는 bodyToMono의 값이 들어오게 된다.
//					log.info("요청 성공 (재시도 횟수 : {})", retryCount.get());
				});
	}

}
