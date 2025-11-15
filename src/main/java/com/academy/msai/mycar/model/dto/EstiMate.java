package com.academy.msai.mycar.model.dto;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EstiMate {
	
	private String rnum;
	private String estimateId;
	private String carId;
	private String carNo;
	private String carAlias;
	private String jsonStr;
	private String estimateDate;
	private String brokenFileMin;
	private String brokenFileMax;
	private String totalRecommendedCostSum;
	
	private ArrayList<BrokenFile> brokenFileList;
	
}
