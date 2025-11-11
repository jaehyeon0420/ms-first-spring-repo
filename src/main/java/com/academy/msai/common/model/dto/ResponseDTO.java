package com.academy.msai.common.model.dto;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseDTO {
									
	private HttpStatus httpStatus;						
	private String clientMsg;
	private Object resData;
	private String alertIcon; 
}

