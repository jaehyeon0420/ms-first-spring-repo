package com.academy.msai.mypage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academy.msai.mypage.model.service.MypageService;

@RestController
@CrossOrigin("*")
@RequestMapping("/mypage")
public class MypageController {
	
	@Autowired
	private MypageService service;
}
