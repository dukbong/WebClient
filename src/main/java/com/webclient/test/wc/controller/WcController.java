package com.webclient.test.wc.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webclient.test.wc.apicomponent.ApiWebClient;
import com.webclient.test.wc.dto.ResponseDto;
import com.webclient.test.wc.dto.TestVo;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class WcController {

	@Autowired
	ApiWebClient apiWebClient;

	@GetMapping("/")
	public List<TestVo> findById() {
		TestVo testvo = new TestVo("reqeust_body_name", "request_body_age");
		ResponseDto result = apiWebClient.postApi("/", testvo);
		log.info("Status = {}", result.getStatus());

		List<TestVo> rslt = new ArrayList<>();
		if (result.getData() instanceof List<?>) { // true
			for (Object obj : (List<?>) result.getData()) {
				if (obj instanceof LinkedHashMap) {
					rslt.add(convertObjToClassType((LinkedHashMap<?, ?>) obj, TestVo.class));
				}
			}
		}
		return rslt;
	}
	
	@GetMapping("/1")
	public Integer findById2() {
		TestVo testvo = new TestVo("ya_name", "ya_age");
		ResponseDto result = apiWebClient.postApi("/1", testvo);
		return (Integer) result.getData();
	}

	/***
	 * 변경을 원하는 클래스 타입으로 지정
	 * 
	 * @param <T>
	 * @param map
	 * @param clazz
	 * @return
	 */
	private <T> T convertObjToClassType(LinkedHashMap<?, ?> map, Class<T> clazz) {
		try {
			log.info("class type = {}", clazz.getDeclaringClass());
			log.info("class type = {}", clazz.getName());
			
			Constructor<T> constructor = clazz.getConstructor();
			T clazzInstance = constructor.newInstance();

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				String key = (String) entry.getKey();
				Object value = entry.getValue();
				try {
					Field field = clazz.getDeclaredField(key);
					field.setAccessible(true); // 필드 접근을 활성화합니다.
					field.set(clazzInstance, value); // 필드에 값을 설정합니다.
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			return clazzInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
