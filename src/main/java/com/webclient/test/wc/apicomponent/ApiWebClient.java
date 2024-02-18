package com.webclient.test.wc.apicomponent;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiWebClient {

	private final WebClient localWebClient;
	private String baseUrl;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public <T> Mono<T> postApi(String uri, Object vo, Class<T> bodyType) {
		
		return localWebClient.post().uri(uri).body(BodyInserters.fromValue(vo)).accept(MediaType.APPLICATION_JSON)
				.retrieve()
				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is4xxClientError, res -> {
					log.error("응답 받은 상태 코드 4xx : {}", res.statusCode());
					return res.createException().flatMap(Mono::error);
				})
				
				
				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is5xxServerError, res -> {
					log.error("응답 받은 상태 코드 5xx : {}", res.statusCode());
					return res.createException().flatMap(Mono::error);
				})
				
				
				// [body] 와 [bodyType]을 매칭시켜서 가져오겠다.
				// WebClient 로직의 핵심 부분이며 해당 부분은 default로 비동기적으로 실행된다.
				.bodyToMono(bodyType)
				
				
				// 재시도 관련 내용
				.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
						// [Filter] 5xx 서버 관련 에러 발생 시 재시도 안함
						// :: 서버 에러가 날 경우 서버에서 조치하기 전엔 재시도 할 필요가 없기 때문이다.
						.filter(error -> !(error instanceof WebClientResponseException
								&& ((WebClientResponseException) error).getStatusCode().is5xxServerError()))
						// 재시도 하기 전 현재 시도에 대한 정보가 담겨 있다.
						.doBeforeRetry(before -> {
							log.error("요청 URL : {}", baseUrl + uri);
//									 	log.error("시간 : {}, 재시도 내용 : {}", LocalTime.now(), before.toString());
							log.error("시간 : {}", LocalTime.now());
							log.error("시도 횟수 : {}", (before.totalRetries() + 1));
							log.error("에러 내용 : {}", before.failure().toString());
//									 	log.error("에러 stack Trace", before.failure());
						}))
				
				
				// 재시도 종료 후 에러가 발생하면 확인할 수 있다.
				.doOnError(error -> {
					log.error("재시도 종료  : {}", error.getMessage());
				});
	}

}
