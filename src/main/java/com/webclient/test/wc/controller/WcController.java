package com.webclient.test.wc.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webclient.test.wc.apicomponent.ApiWebClient;
import com.webclient.test.wc.dto.ResponseDto;
import com.webclient.test.wc.dto.TestVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
//@ApiAop(methods = {"findById"})
@RequiredArgsConstructor
public class WcController {
	
	private final ApiWebClient apiWebClient;
	
	@Value("${api.baseUrl}")
	private String baseUrl;
	/***
	 * 동적으로 baseUrl을 지정하여 각 컨트롤러에서 각기 다른 url로 통신할 수 있도록 만
	 * @param apiWebClient
	 * @param baseUrl
	 */

	/***
	 * StatusResponseDto와 ResponseDto의 역할은 다르다.
	 * @return
	 */
	@GetMapping("/")
	public ResponseEntity<ResponseDto> findById(HttpServletRequest request) {
		
		String ip = request.getLocalAddr();
		log.info("=========================> {}", ip);
		
		// userId와 현재 URL을 반환
//		apiWebClient.setRequestInfo(baseUrl);
		apiWebClient.setCookie("UserId", request.getRequestURL().toString(), baseUrl);
		TestVo testvo = new TestVo("reqeust_body_name", "request_body_age");
		ResponseDto result = apiWebClient.request(HttpMethod.POST,"/", testvo, "testToken" ,ResponseDto.class).block();
		
		// [Object]를 안전하게 바꾸는 형변환하기
		List<TestVo> rslt = new ArrayList<>();
		if (result.getData() instanceof List<?>) { // true
			for (Object obj : (List<?>) result.getData()) {
				if (obj instanceof LinkedHashMap) {
					rslt.add(convertObjToClassType((LinkedHashMap<?, ?>) obj, TestVo.class));
				}
			}
		}
		return ResponseEntity.ok().body(result);
	}
	
	@GetMapping("/2")
	public TestVo timeTest(HttpServletRequest request) {
		String ip = request.getLocalAddr();
//		apiWebClient.setRequestInfo(baseUrl);
		apiWebClient.setCookie("UserId", request.getRequestURL().toString(), baseUrl);
		TestVo testvo = new TestVo("reqeust_body_name", "request_body_age");
		ResponseDto result = apiWebClient.request(HttpMethod.POST,"/2", testvo, "testToken", ResponseDto.class).block();
		TestVo resTestVo = new TestVo(String.valueOf(result.getStatus()), (String)result.getData());
		return resTestVo;
	}
	
	@GetMapping("/3")
	public ResponseEntity<ResponseDto> getTest(HttpServletRequest request) {
		
		String ip = request.getLocalAddr();
		log.info("=========================> {}", ip);
		
		// userId와 현재 URL을 반환
//		apiWebClient.setRequestInfo(baseUrl);
		apiWebClient.setCookie("UserUser", request.getRequestURL().toString(), baseUrl);
		
		Map<String, String> map = new HashMap<>();
		map.put("test","testui");
		
		ResponseDto result = apiWebClient.request(HttpMethod.GET, "/3", map, "testToken", ResponseDto.class).block();
		
		return ResponseEntity.ok().body(result);
		
	}
	
//	@GetMapping("/1")
//	public Integer findById2() {
//		TestVo testvo = new TestVo("ya_name", "ya_age");
//		ResponseDto result = apiWebClient.postApi("/1", testvo);
//		return (Integer) result.getData();
//	}

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