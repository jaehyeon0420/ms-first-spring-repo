package com.academy.msai.member.model.dto;

import java.util.ArrayList;

import com.academy.msai.mycar.model.dto.Car;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Member {
	private String memberId;
	private String memberPw;
	private String memberName;
	private String memberPhone;
	
	//차량 정보 리스트
	private ArrayList<Car> carList;	
}

