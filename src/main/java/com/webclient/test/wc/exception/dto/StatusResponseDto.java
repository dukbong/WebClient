package com.webclient.test.wc.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatusResponseDto {
    private Integer status;
    private Object data;

    public StatusResponseDto(Integer status) {
        this.status = status;
    }

    public static StatusResponseDto addStatus(Integer status) {
        return new StatusResponseDto(status);
    }

    public static StatusResponseDto success(){
        return new StatusResponseDto(200);
    }
    public static StatusResponseDto success(Object data){
        return new StatusResponseDto(200, data);
    }
    public static StatusResponseDto serverFail(Integer status, Object data){
        return new StatusResponseDto(status, data);
    }
}
