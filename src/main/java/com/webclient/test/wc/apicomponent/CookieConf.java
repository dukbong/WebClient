package com.webclient.test.wc.apicomponent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConf {
	
	@Bean
	public InmemoryCookie cookieStore() {
		return new InmemoryCookie();
	}
}
