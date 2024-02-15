package com.webclient.test.wc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/***
 * 해당 DTO는 요청시 넘어오는 Json 형태에 따라 키값으로 변수명을 지정해줘야한다.
 * @author jkmo2
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Test {

	Integer status;
	Object data;
	String show;
	
}
