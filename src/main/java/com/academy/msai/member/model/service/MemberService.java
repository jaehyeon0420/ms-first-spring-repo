package com.academy.msai.member.model.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.msai.common.util.JwtUtils;
import com.academy.msai.member.model.dao.MemberDao;
import com.academy.msai.member.model.dto.LoginMember;
import com.academy.msai.member.model.dto.Member;
import com.academy.msai.mycar.model.dao.MycarDao;
import com.academy.msai.mycar.model.dto.Car;


@Service
public class MemberService {
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private MycarDao mycarDao;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Autowired
	private JwtUtils jwtUtils;

	@Transactional
	public int insertMember(Member member) {
		String encodePw = encoder.encode(member.getMemberPw());
		member.setMemberPw(encodePw);
		
		int result = memberDao.insertMember(member);
		
		ArrayList<Car> carList = member.getCarList(); 		
		
		for(int i=0; i<carList.size(); i++) {
			//차 ID(시퀀스) 조회
			String carId = mycarDao.getCarId();
			
			//차량정보에 차량 ID와 회원 아이디 세팅
			carList.get(i).setCarId(carId);
			carList.get(i).setMemberId(member.getMemberId());
			
			
			//차량 정보 등록 후, 차량 이미지 정보 등록
			mycarDao.insertCar(carList.get(i));
			
		}
		
		
		return result;
	}

	public int idDuplChk(String memberId) {
		return memberDao.idDuplChk(memberId);
	}

	public LoginMember memberLogin(Member member) {
		//아이디만 전달
		Member selectMember = memberDao.memberLogin(member.getMemberId());
		
		//아이디 잘못 입력 시, 회원정보가 없으니 null이 리턴됨. 패스워드 검사 이전에 null 처리
		if(selectMember == null) {
			return null;
		}
		
		//입력 비밀번호와 DB에 저장된 암호화 비밀번호 검증
		if(encoder.matches(member.getMemberPw(), selectMember.getMemberPw())) {
			String accessToken = jwtUtils.createAccessToken(selectMember.getMemberId());
			String refreshToken = jwtUtils.createRefreshToken(selectMember.getMemberId());
			
			//비밀번호는 프론트에서 보관하지 않을 것이므로 null 처리
			selectMember.setMemberPw(null);
			LoginMember loginMember = new LoginMember(accessToken, refreshToken, selectMember);
			return loginMember;
		}else {			
			return null;
		}
	}
	
	//refreshToken으로 accessToken 재발급 요청 - AOP 적용 이후
	
	public String refreshToken(Member member) {
		//전달받은 아이디와 레벨로 액세스 토큰 재발급	
		String reAccessToken = jwtUtils.createAccessToken(member.getMemberId());
		return reAccessToken;
		
	}
	
	
	//마이페이지 - AOP 적용 이후 - 매퍼에서 조회 컬럼 중, 비밀번호 제거할 것
	public Member selectOneMember(String memberId) {
		Member member = memberDao.selectOneMember(memberId);
		return member;
	}
	
	//회원 정보 수정 - AOP 적용 이후
	@Transactional
	public int updateMember(Member member) {
		return memberDao.updateMember(member);
		
	}
	
	
	//비밀번호 변경 전, 기존 비밀번호 체크 - AOP 적용 이후
	public boolean chkMemberPw(Member member) {
		Member m = memberDao.selectOneMember(member.getMemberId()); 
		
		//평문 비밀번호와 암호화 비밀번호 일치성 검증
		if(encoder.matches(member.getMemberPw(), m.getMemberPw())) {
			return true;
		}
		
		return false;
	}
		
	//비밀번호 변경 - AOP 적용 이후
	@Transactional
	public int updateMemberPw(Member member) {			
		//입력 비밀번호 암호화 처리 후, 재할당
		String encodePw = encoder.encode(member.getMemberPw());
		member.setMemberPw(encodePw);
		
		int result = memberDao.updateMemberPw(member); 
		
		return result;
		
	}
	
	//회원 탈퇴 - AOP 적용 이후
	@Transactional
	public int deleteMember(String memberId) {
		int result = memberDao.deleteMember(memberId); 
		
		return result;
	}

}
