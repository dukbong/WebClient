package com.webclient.test.wc.apicomponent;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webclient.test.wc.exception.dto.DbInsertInfo;

import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/***
 * 해결해야 하는 사항 1. 어떤 로그를 남길것인가? api 호출 시 이력을 남김 성공 : doOnSuccess 안에서 로그 기록 실패 :
 * doBeforeRetry 안에서 로그 기록 > doBeforeRetry : 여기서 남길 경우 첫 통신 외 재시도한 경우까지 기록 1. 실제
 * 응답받은 에러는 해당 메소드에서 확인된다. 2. 시간 초과 관련 WebClient에서 설정한 오류도 여기서 확인이 가능하다. >
 * doOnError : 재시도가 종료된 후 관련 WebClient 입장에서 발생하는 에러를 알 수 있다. (ex : Retries
 * exhausted: 3/3)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiWebClient {

	private final WebClient localWebClient;
	private String baseUrl;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String cookie;
	private String mainUrl;
	
	public void setRequestInfo(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void setCookie(String userId, String requestUrl) {
		this.mainUrl = requestUrl;
		this.cookie = new StringBuffer().append("userSn=").append(userId).append("; ").append("url=").append(requestUrl).toString();
	}
	
	private int portSearch(String targetUrl) {
		URI uri = URI.create(targetUrl);
		return uri.getPort();
	}
	
	private String getSubjectName(int portNum) {
		SubjectDivision[] subjects  = SubjectDivision.values();
		for(SubjectDivision subject : subjects) {
			if(subject.getPortNum() == portNum) {
				return subject.getSubjectName();
			}
		}
		return "UNKNOWN";
	}
	
	public <T> Mono<T> postApi(String uri, Object vo, String token, Class<T> bodyType) {
		DbInsertInfo dbInfo = new DbInsertInfo();
		// 동시성 문제를 해결하고 시도한 횟수를 알기 위한 변수
//		AtomicInteger retryCount = new AtomicInteger(1);
		String targetSubject = getSubjectName(portSearch(baseUrl));
		String mainSubject = getSubjectName(portSearch(mainUrl));
		dbInfo.from(mainSubject).to(targetSubject).url(baseUrl + uri).param(objectConvertJson(vo)).count(0);
		
		cookie = new StringBuffer().append(cookie).append("; ")
								   .append("to=").append(mainSubject).append("; ")
								   .append("from=").append(targetSubject).append("; ")
								   .append("count=").append(dbInfo.getCount()).append("; ")
								   .toString();
		// WebClient는 불변객체이므로 직접 변경은 불가능하다.
		WebClient useClient = localWebClient.mutate().baseUrl(baseUrl).build();
		
		return useClient.post().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).header(HttpHeaders.COOKIE, cookie)
				.body(BodyInserters.fromValue(vo)).accept(MediaType.APPLICATION_JSON).retrieve()
				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is4xxClientError, res -> {
//					log.info("현재 URL : {}", baseUrl + uri);
					log.error("응답 받은 상태 코드 4xx : {}", res.statusCode());
					res.bodyToMono(bodyType).doOnNext(body -> {
						dbInfo.message(objectConvertJson(body)).flag("Y").countPlus();
//						String resMessage = objectConvertJson(body);
//						String param = objectConvertJson(vo);
						log.info(
								"DB Insert [FAILD 4XX] ==> 요청시간 : {}, 요청 횟수 : {},  From : {}, To : {}, 요청ID : {},  url : {}, IP : {},  구분 : {}, param : {}, 응답 메시지 {}",
								dbInfo.getTime(), dbInfo.getCount(), dbInfo.getFrom(), dbInfo.getTo(), "요청 ID 아직 없음", dbInfo.getUrl(), "IP 아직 없음", dbInfo.getFlag(), dbInfo.getParam(), dbInfo.getMessage());
					}).subscribe();
					// 응답 헤더 값 확인 (JWT TOKEN)
//					log.error("header = {}", res.headers().asHttpHeaders());
					return res.createException().flatMap(Mono::error);
				})

				// [응답 서버로 부터 받은 에러를 log로 찍고 오류를 전파시키기]
				.onStatus(HttpStatus::is5xxServerError, res -> {
//					log.info("현재 URL : {}", baseUrl + uri);
					log.error("응답 받은 상태 코드 5xx : {}", res.statusCode());
					res.bodyToMono(bodyType).doOnNext(body -> {
						dbInfo.message(objectConvertJson(body)).flag("Y").countPlus();
//						String resMessage = objectConvertJson(body);
//						String param = objectConvertJson(vo);
						log.info(
								"DB Insert [FAILD 5XX & RETRY X] ==> 요청시간 : {}, 요청 횟수 : {}, From : {}, To : {}, 요청ID : {},  url : {}, IP : {},  구분 : {}, param : {}, 응답 메시지 {}",
								dbInfo.getTime(), dbInfo.getCount(), dbInfo.getFrom(), dbInfo.getTo(), "요청 ID 아직 없음", dbInfo.getUrl(), "IP 아직 없음", dbInfo.getFlag(), dbInfo.getParam(), dbInfo.getMessage());
					}).subscribe();
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
									&& error.getCause() instanceof TimeoutException;

							// 5xx 서버 에러이거나 ReadTimeoutException이 아닌 경우에만 true를 반환
							return !is5xxServerError && !isReadTimeoutException;
						})
						// 재시도 하기 전 현재 시도에 대한 정보가 담겨 있다.
						.doBeforeRetry(before -> {
//							dbInfo.timeReset();
//							log.error("요청 시간 : {}", LocalTime.now());
//							log.error("요청 URL : {}", baseUrl + uri);
//							WebClientResponseException webClientResponseException = null;
//							if (before != null && before.failure() instanceof WebClientResponseException) {
//								webClientResponseException = (WebClientResponseException) before.failure();
//								log.error("상태 코드 값  : {}", webClientResponseException.getRawStatusCode());
//								log.error("에러 내용     : {}", before.failure().toString());
//								log.error("요청 시도 횟수 : {}", (before.totalRetries() + 1));
//								log.error("에러 내용     : {}", before.failure().toString());
//							}
//							if (webClientResponseException != null) {
//								log.error("상태 코드 값  : {}", webClientResponseException.getStatusCode().value());
//							}
							// [재시도 관련 모든 내용을 포함]
//							 log.error("시간 : {}, 재시도 내용 : {}", LocalTime.now(), before.toString());
//							 log.error(" test = {}",(before.totalRetries() + 1) );
							// [오류의 상세 내용]
							// log.error("에러 stack Trace", before.failure());

							// [성공시 몇번만에 성공했는지 알고 싶다면 사용 가능]
//							retryCount.incrementAndGet();
//							dbInfo.count(String.valueOf(retryCount));
				            cookie = new StringBuffer(cookie)
				                    .append("; ")
				                    .append("count=")
				                    .append(dbInfo.getCount())
				                    .toString();
				            
						}))
				// 재시도 종료 후 에러가 발생하면 확인할 수 있다.
				.doOnError(error -> {
					log.info("uri = {}", baseUrl + uri);
					log.error("1. 재시도 종료  : {}", error.getMessage());
				}).doOnSuccess(success -> {
//					String param = objectConvertJson(vo);
					dbInfo.flag("N").message(objectConvertJson(success)).countPlus();
					log.info("DB Insert [SUCCESS] ==> 요청시간 : {}, 요청 횟수 : {}, From : {}, To : {}, 요청 ID : {}, url : {}, ip : {},  구분 : {}, param : {}, 응답 메시지 : {}",
//													LocalDate.now(), mainSubject, targetSubject, "요청 ID 아직 없음", (baseUrl + uri), "IP 아직 없음", 'N', param, success.toString());
													dbInfo.getTime(), dbInfo.getCount(), dbInfo.getFrom(), dbInfo.getTo(),"요청 ID", dbInfo.getUrl(), "IP",dbInfo.getFlag(),dbInfo.getParam(), dbInfo.getMessage());
				});
	}
	
	
	public String objectConvertJson(Object obj){
		try {
			return objectMapper.writeValueAsString(obj);
		}catch(JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	// 4xx, 5xx >> DB INSERT METHOD (미완성)
//	public <T> Disposable resBodyResult(ClientResponse response, Class<T> bodyType, Object requestBody) {
//		return 
//			response.bodyToMono(bodyType).doOnNext(body -> {
//			String bodyMessage = objectConvertJson(body);
//			String param = objectConvertJson(requestBody);
//			
//		}).subscribe();
//	}

}
