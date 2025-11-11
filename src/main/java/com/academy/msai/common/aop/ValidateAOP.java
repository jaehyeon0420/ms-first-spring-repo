package com.academy.msai.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.academy.msai.common.exception.CommonException;
import com.academy.msai.common.util.JwtUtils;
import com.academy.msai.member.model.dto.Member;

import jakarta.servlet.http.HttpServletRequest;

@Component	
@Aspect 	
public class ValidateAOP {
	
		
	@Autowired
	private JwtUtils jwtUtils;
	
	//controller 패키지 내부 파일들에 작성된 모든 메소드
	@Pointcut("execution(* kr.or.iei.*.controller.*.*(..))")
	public void allControllerPointCut() {}
		
	//NoLoginChk 어노테이션
	@Pointcut("@annotation(kr.or.iei.common.annotation.NoTokenCheck)")
	public void noTokenCheckAnnotation() {}
	
	//모든 Controller 모든 메소드 중, NoTokenCheck 어노테이션이 작성되지 않은 메소드 
	@Before("allControllerPointCut() && !noTokenCheckAnnotation()")
	public void validateCheck() {
		HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
		
		//요청 헤더에 포함된 토큰 추출(url에 따라 리프레시 토큰 또는 액세스 토큰 추출)
		//URI ex : /admin/member
		//URL ex : http://localhost:9999/admin/member
		String uri = request.getRequestURI();
		
		String token = uri.endsWith("refresh") 
				       ? request.getHeader("refreshToken") 
				       : request.getHeader("Authorization");
		
		
		
		//토큰 유효성 검증 실패 시, 예외를 발생시킴 => Before는 컨트롤러 이전에 실행되므로 예외가 컨트롤러로 전달되지 않음. => 예외처리기 작성
		//헤더에 토큰이 포함되지 않아 null이여도 validate에서 Exception 발생.
		//이제 모든 컨트롤러, 서비스 메소드에서 토큰 검증 및 검증 결과에 따른 응답 처리 부분 제거
		Object resObj = jwtUtils.validateToken(token);
		
		if(resObj instanceof HttpStatus httpStatus) {
			CommonException ex = new CommonException("invalid jwtToken in request Header");
			ex.setErrorCode(httpStatus);
			throw ex;
			
		}
		
		if(resObj instanceof Member member){//토큰 검증 결과 Object가 Member일 때
			
		}
		
	}
	
}
