package com.academy.msai.mypage.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.msai.member.model.dao.MemberDao;

@Service
public class MypageService {
	
	@Autowired
	private MemberDao dao;
}
