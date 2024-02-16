package com.webclient.test.wc.apicomponent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiWebBuilder {

	@Bean
	public WebClient localWebClient() {
		return WebClient.builder().baseUrl("http://localhost:8099").build();
	}
}
