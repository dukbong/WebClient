package com.webclient.test.wc.apicomponent;

import java.net.CookieManager;
import java.net.CookieStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConf {

	@Bean
	public CookieManager cookieManager() {
		return new CookieManager();
	}
	
	@Bean
	public CookieStore cookieStore(CookieManager cookieManager) {
		return cookieManager.getCookieStore();
	}
	
}
