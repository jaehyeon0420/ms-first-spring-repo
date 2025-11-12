package com.academy.msai.mycar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BrokenFile {
	
	private String brokenFileNo;
	private String carId;
	private String brokenFileName;
	private String brokenFilePath;
}
