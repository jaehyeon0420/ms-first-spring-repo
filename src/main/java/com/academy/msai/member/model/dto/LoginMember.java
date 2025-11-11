package com.academy.msai.member.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginMember {
	private String accessToken;
	private String refreshToken;
	private Member member;
}
