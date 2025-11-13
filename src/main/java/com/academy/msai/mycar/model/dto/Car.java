package com.academy.msai.mycar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Car {
	private String carId;
	private String carNo;
	private String carKind;
	private String carAlias;
	private String memberId;
	private String carFileName;
	private String carFilePath;

}
