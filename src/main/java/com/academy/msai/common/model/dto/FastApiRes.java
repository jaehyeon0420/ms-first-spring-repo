package com.academy.msai.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FastApiRes {
	
	private String breakage;
	private String crushed;
	private String scratched;
	private String separate;
}
