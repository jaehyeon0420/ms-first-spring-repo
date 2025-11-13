package com.academy.msai.member.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.academy.msai.common.annotation.NoTokenCheck;
import com.academy.msai.common.model.dto.ResponseDTO;
import com.academy.msai.common.util.FileUtils;
import com.academy.msai.member.model.dto.LoginMember;
import com.academy.msai.member.model.dto.Member;
import com.academy.msai.member.model.service.MemberService;
import com.academy.msai.mycar.model.dto.Car;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin("*")
@RequestMapping("/members")
public class MemberController {

	@Autowired
	private MemberService memberService;
	
	@Autowired
	private FileUtils fileUtil;
	@Value("${spring.datasource.url}")
	private String dbUrl;
	//테스트
	@GetMapping
	@NoTokenCheck
	public String test() {
		String test = memberService.test();
		return dbUrl+test;
	}
	
	@PostMapping //등록 == POST
	@NoTokenCheck //로그인 체크 X
	public ResponseEntity<ResponseDTO> memberJoin(@ModelAttribute Member member,
												@ModelAttribute MultipartFile [] carFiles){
		//실패했을 때 응답 객체 초기 세팅
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			//차량's 이미지 처리
			ArrayList<Car> carList = member.getCarList();
			
			for(int i=0; i<carFiles.length; i++) {
				String filePath = fileUtil.uploadFile(carFiles[i], "/car/");
				
				Car car = carList.get(i);
				car.setCarFileName(carFiles[i].getOriginalFilename());
				car.setCarFilePath(filePath);
			}
			
			int result = memberService.insertMember(member);
			
			if(result > 0) {
				res = new ResponseDTO(HttpStatus.OK, "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.", true, "success");
			}else {
				res = new ResponseDTO(HttpStatus.OK, "회원가입 중, 오류가 발생하였습니다.", false, "warning");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
		
	}
	
		
	//아이디 중복체크
	@GetMapping("/{memberId}/id-check")
	@NoTokenCheck //로그인 체크 X
	public ResponseEntity<ResponseDTO> idDuplChk(@PathVariable String memberId) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "아이디 중복 체크 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			int count = memberService.idDuplChk(memberId);
			
			res = new ResponseDTO(HttpStatus.OK, "", count, "");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	@PostMapping("/login")
	@NoTokenCheck
	public ResponseEntity<ResponseDTO> memberLogin(@RequestBody Member member){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 시도 중, 오류가 발생하였습니다.", null, "error");
		
		try {
			//예외 발생 시, 초기 res 변수 세팅값으로 응답!
			LoginMember loginMember = memberService.memberLogin(member);
			
			if(loginMember != null) {
				res = new ResponseDTO(HttpStatus.OK, "", loginMember, ""); //axios 인터셉터에서 clientMsg 존재시에만, alert 띄우므로 아이콘 설정 불필요
			}else {
				res = new ResponseDTO(HttpStatus.OK, "아이디 및 비밀번호를 확인하세요.", null, "warning");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	
	@PostMapping("/refresh")
	public ResponseEntity<ResponseDTO> refreshToken(@RequestBody Member member) {
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "", null, "error");
		
		try {
			//정상적으로 accessToken이 발급된 경우
			String reAccessToken = memberService.refreshToken(member);
			res = new ResponseDTO(HttpStatus.OK, "", reAccessToken, "");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	
	//마이페이지 - AOP 적용 이후
	@GetMapping("/{memberId}")
	public ResponseEntity<ResponseDTO> selectOneMember(@PathVariable String memberId){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "정보 조회 중, 오류가 발생하였습니다.", null, "error");
		
		try {
			Member member = memberService.selectOneMember(memberId);
			
			if(member != null) {
				res = new ResponseDTO(HttpStatus.OK, "", member, "");				
			}else {
				res = new ResponseDTO(HttpStatus.OK, "회원 정보가 존재하지 않습니다.", null, "warning");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	//회원 정보 수정 - AOP 적용 이후
	@PatchMapping
	public ResponseEntity<ResponseDTO> updateMember(@RequestBody Member member){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "수정 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			int result = memberService.updateMember(member);
			
			if(result > 0) {
				res = new ResponseDTO(HttpStatus.OK, "회원 정보 수정이 완료되었습니다.", true, "success");				
			}else {
				res = new ResponseDTO(HttpStatus.OK, "수정된 회원이 존재하지 않습니다.", false, "warning");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	
	//비밀번호 변경 이전, 기존 비밀번호 체크 - AOP 적용 이후
	@PostMapping("/auth/password-check")
	public ResponseEntity<ResponseDTO> chkMemberPw(@RequestBody Member member){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "기존 비밀번호 체크 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			
			boolean result = memberService.chkMemberPw(member);
			
			//비밀번호 일치 결과 리턴
			if(result) {
				res = new ResponseDTO(HttpStatus.OK, "기존 비밀번호가 일치합니다. 변경할 비밀번호를 입력하세요.", result, "success");				
			}else {
				res = new ResponseDTO(HttpStatus.OK, "기존 비밀번호가 일치하지 않습니다. ", result, "warning");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	
	//비밀번호 변경 - AOP 적용 이후
	@PatchMapping("/password")
	public ResponseEntity<ResponseDTO> updateMemberPw(@RequestBody Member member){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 변경 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			
			int result = memberService.updateMemberPw(member);
			
			if((Integer) result > 0) {
				res = new ResponseDTO(HttpStatus.OK, "비밀번호가 정상적으로 변경 되었습니다. 변경된 비밀번호로 다시 로그인 하시길 바랍니다.", true, "success");					
			}else {
				res = new ResponseDTO(HttpStatus.OK, "비밀번호 중, 오류가 발생하였습니다.", false, "warning");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
	
	
	//회원 탈퇴 - AOP 적용 이후
	@DeleteMapping("/{memberId}")
	public ResponseEntity<ResponseDTO> deleteMember(@PathVariable String memberId){
		ResponseDTO res = new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "회원 탈퇴 중, 오류가 발생하였습니다.", false, "error");
		
		try {
			
			int result = memberService.deleteMember(memberId);
			
			if(result > 0) {
				res = new ResponseDTO(HttpStatus.OK, "회원 탈퇴가 완료되었습니다.", true, "success");
			}else {
				res = new ResponseDTO(HttpStatus.OK, "회원 탈퇴 중, 오류가 발생하였습니다.", false, "warning");
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<ResponseDTO>(res, res.getHttpStatus());
	}
}
