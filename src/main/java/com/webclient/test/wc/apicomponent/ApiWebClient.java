package com.webclient.test.wc.apicomponent;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.webclient.test.wc.dto.ResponseDto;
import com.webclient.test.wc.exception.CustomException;
import com.webclient.test.wc.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiWebClient {
	
	private final WebClient localWebClient;

	public ResponseDto postApi(String uri, Object vo){
		return localWebClient.post().uri(uri).body(BodyInserters.fromValue(vo)).accept(MediaType.APPLICATION_JSON) // Json형태로 받아올거다.
				.exchangeToMono(res -> {
					if (res.statusCode().equals(HttpStatus.OK)) {
						log.info("    pass => {}", res.statusCode());
						return res.bodyToMono(ResponseDto.class);
					} else {
						log.error("Non Pass & Status = {}", res.statusCode());
						return Mono.error(new CustomException(ErrorCode.TEST_400));
					}
				}).retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
						.filter(throwable -> throwable instanceof RuntimeException)).block();
	}
	
}
