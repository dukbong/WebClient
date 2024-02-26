package com.webclient.test.wc.apicomponent;


import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ApiWebBuilder {
	
	@Bean
	public WebClient localWebClient() {
		
		/***
		 * 응답 제한 시간 : 5초 [최종적으로 응답을 받기 까지의 시간 제한]
		 * 연결 제한 시간 : 3초 [요청 서버와 응답 서버의 연결 시간 제한]
		 * 읽기 제한 시간 : 5초 [요청을 받는 쪽과 연관]
		 * 쓰기 제한 시간 : 5초 [응답을 받는 쪽과 연관]
		 * 
		 * 응답 제한 시간은 읽기와 쓰기 중 긴것을 선택해야 한다.
		 */
		HttpClient client = HttpClient
										.create()
										// [응답 시간 제한]
										.responseTimeout(Duration.ofSeconds(5))
										// [연결 시간 제한]
										.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
//									  	===== deprecated
//									  	.doOnConnected(conn -> conn
//											  // [읽기 시간 제한]
//											  .addHandler(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
//											  // [쓰기 시간 제한]
//											  .addHandler(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));
		
										.doOnChannelInit((observer, channel, address) -> {
											// [읽기 시간 제한]
										    channel.pipeline().addLast(new ReadTimeoutHandler(5));
										    // [쓰기 시간 제한]
										    channel.pipeline().addLast(new WriteTimeoutHandler(5));
										 });
		
		return WebClient.builder()
//						.baseUrl("http://localhost:8099")
						// [max in memory] default = 256KB
						.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
						.clientConnector(new ReactorClientHttpConnector(client))
						.build();
	}
}
