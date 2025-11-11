package com.academy.msai.member.model.dto;

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
}

