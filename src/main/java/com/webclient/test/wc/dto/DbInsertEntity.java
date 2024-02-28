package com.webclient.test.wc.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자(NoArgsConstructor)의 접근 제어를 PROCTECTED 로 설정하면 아무런 값도 갖지 않는 의미 없는 객체의 생성을 막게 됩니다. 
@SequenceGenerator(name = "dbinsertentity_generator", sequenceName = "db_seq")
public class DbInsertEntity {

	@Id
	@GeneratedValue (generator = "dbinsertentity_generator")
	private Long id;
	
	private String requestTime;
	
	private Integer count;
	
	private String ownerFrom;
	
	private String sendTo;
	
	private String userSn;
	
	private String userIp;
	
	private String url;
	
	private String flag;
	
	private String param;
	
	@Lob
	private String message;
	
	
	
	public void updateMessage(String msg) {
		this.message = msg;
	}


	@Builder
	public DbInsertEntity(Long id, String requestTime, Integer count, String ownerFrom, String sendTo, String userSn,
			String userIp, String url, String flag, String param, String message) {
		this.id = id;
		this.requestTime = requestTime;
		this.count = count;
		this.ownerFrom = ownerFrom;
		this.sendTo = sendTo;
		this.userSn = userSn;
		this.userIp = userIp;
		this.url = url;
		this.flag = flag;
		this.param = param;
		this.message = message;
	}
}
