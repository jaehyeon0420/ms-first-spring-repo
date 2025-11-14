package com.academy.msai.mycar.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.academy.msai.common.model.dto.ResponseDTO;
import com.academy.msai.mycar.model.dto.Car;
import com.academy.msai.mycar.model.service.MycarService;

@RestController
@CrossOrigin("*")
@RequestMapping("/mycar")
public class MycarController {
	
	@Autowired
	private MycarService service;
	
	//내 차 리스트 조회(페이징)
	@GetMapping
	public ResponseEntity<ResponseDTO> getCarList(@RequestParam int reqPage, @RequestParam String memberId) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "차량 정보 조회 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			HashMap<String, Object> carMap = service.selectCarList(reqPage, memberId);
			res = new ResponseDTO(HttpStatus.OK, "", carMap, "");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	//내 차 리스트 조회(전체)
	@GetMapping("/all")
	public ResponseEntity<ResponseDTO> getAllCarList(@RequestParam String memberId) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "차량 정보 전체 조회 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			ArrayList<Car> carList = service.selectAllCarList(memberId);
			res = new ResponseDTO(HttpStatus.OK, "", carList, "");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	//견적 요청
	@PostMapping
	public ResponseEntity<ResponseDTO> reqEsimate(@RequestParam String carId, @ModelAttribute MultipartFile [] brokenFiles) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "견적 요청 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			
			int reuslt = service.insertEsimateHist(carId, brokenFiles);
			
			//로딩 화면을 보여주기 위한 딜레이
			Thread.sleep(3000);
			
			if(reuslt > 0) {
				res = new ResponseDTO(HttpStatus.OK, "수리비 견적 예측이 완료되었습니다.", true, "success");
			}else {
				res = new ResponseDTO(HttpStatus.OK, "견적 요청 중, 오류가 발생하였습니다.", false, "warning");
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	//견적 이력 조회(페이징)
	@GetMapping("/estimate")
	public ResponseEntity<ResponseDTO> getEstiMateList(@RequestParam int reqPage, @RequestParam String memberId) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "견적 이력 조회 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			HashMap<String, Object> estiMateMap = service.selectEstiMateList(reqPage, memberId);
			res = new ResponseDTO(HttpStatus.OK, "", estiMateMap, "");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
}
