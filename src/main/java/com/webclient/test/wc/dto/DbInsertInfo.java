package com.webclient.test.wc.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@NoArgsConstructor
@ToString
public class DbInsertInfo {

	private String time = LocalDateTime.now().toString();
	private String method;
	private String from;
	private Integer count;
	private String to;
	private String userSn;
	private String url;
	private String ip;
	private String flag;
	private String param;
	private String message;
	
    public DbInsertInfo method(String method) {
        this.method = method;
        return this;
    }

	
	public DbInsertInfo timeReset() {
		this.time = LocalDateTime.now().toString();
		return this;
	}
	
    public DbInsertInfo count(int count) {
        this.count = count;
        return this;
    }
    
    // 시도 횟수를 남기기 위함
    public DbInsertInfo countPlus() {
        this.count = this.count + 1;
        return this;
    }

    public DbInsertInfo from(String from) {
        this.from = from;
        return this;
    }

    public DbInsertInfo to(String to) {
        this.to = to;
        return this;
    }

    public DbInsertInfo userSn(String userSn) {
        this.userSn = userSn;
        return this;
    }

    public DbInsertInfo url(String url) {
        this.url = url;
        return this;
    }
    
    public DbInsertInfo ip(String ip) {
        this.ip = ip;
        return this;
    }
    
    public DbInsertInfo flag(String flag) {
        this.flag = flag;
        return this;
    }
    
    public DbInsertInfo param(String param) {
        this.param = param;
        return this;
    }
    
    public DbInsertInfo message(String message) {
        this.message = message;
        return this;
    }
    
    public DbInsertEntity covertEntity() {
    	return DbInsertEntity.builder()
    						 .method(this.method)
    						 .requestTime(this.time)
    						 .userIp(this.ip)
    						 .count(this.count)
    						 .ownerFrom(this.from)
    						 .sendTo(this.to)
    						 .userSn(this.userSn)
    						 .url(this.url)
    						 .flag(this.flag)
    						 .param(this.param)
    						 .message(this.message)
    						 .build();
    }
    
}
