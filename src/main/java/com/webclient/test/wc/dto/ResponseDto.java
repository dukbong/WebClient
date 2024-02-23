package com.webclient.test.wc.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/***
 *  >> Jackson이 Json의 키와 필드이름이 일치하면 해당 내용을 매핑시켜준다.
 * 해당 DTO는 요청시 넘어오는 Json 형태에 따라 키값으로 변수명을 지정해줘야한다.
 * 단, 해당 Dto를 사용하는 경우 응답받는 Dto와 같은 필드값을 가진 객체로 만들어주는것이 좋다.
 * 
 * 
 * 즉, 응답 해주는 곳에서는 status와 data를 주는 StatusResponseDto를 만들어 줘야 한다.
 * @author jkmo2
 */
@Getter
// Jackson이 객체를 역직열화할때 필수로 기본생성자를 사용하기 때문에 필수 이다.
@NoArgsConstructor
@ToString
public class ResponseDto {
	
	private String status;
	private Object data;
	
	ResponseDto(String status, Class<?> data){
		this.status = status;
		this.data = data;
	}
}
