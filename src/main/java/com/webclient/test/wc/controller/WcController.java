package com.webclient.test.wc.controller;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.webclient.test.wc.dto.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RestController
@Slf4j
public class WcController {

	@GetMapping("/")
	public Mono<Test> findById() {
		WebClient client = WebClient.builder().baseUrl("http://localhost:8099").build();
		return client.get().uri("/").accept(MediaType.APPLICATION_JSON) // Json형태로 받아올거다.
				.exchangeToMono(res -> {
					if (res.statusCode().equals(HttpStatus.OK)) {
						log.info("    pass => {}", res.statusCode());
						return res.bodyToMono(Test.class);
					} else {
						log.info("Non Pass => {}", res.statusCode());
						/***
						 * CustomException 만들어서 넣기
						 */
						return Mono.error(new RuntimeException("HTTP Status Code: " + res.statusCode()));
					}
				}).retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
						.filter(throwable -> throwable instanceof RuntimeException));
	}
}
