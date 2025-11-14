package com.academy.msai.mycar.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.academy.msai.common.model.dto.FastApiRes;
import com.academy.msai.common.model.dto.PageInfo;
import com.academy.msai.common.util.FileUtils;
import com.academy.msai.common.util.PageUtil;
import com.academy.msai.member.model.dao.MemberDao;
import com.academy.msai.mycar.model.dao.MycarDao;
import com.academy.msai.mycar.model.dto.BrokenFile;
import com.academy.msai.mycar.model.dto.Car;
import com.academy.msai.mycar.model.dto.EstiMate;

@Service
public class MycarService {
	
	@Autowired
	private MycarDao dao;
	
	@Autowired
	private PageUtil pageUtil;
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private FileUtils fileUtil;
	
	@Value("${fastapi.endpoint}")
	private String endpoint;

	public HashMap<String, Object> selectCarList(int reqPage, String memberId) {
		int viewCnt = 10;							//한 페이지당 게시물 수
		int pageNaviSize = 5;						//페이지 네비게이션 길이
		int totalCount = dao.selectCarCount(memberId);	//전체 게시글 수
		
		//페이징 정보
		PageInfo pageInfo = pageUtil.getPageInfo(reqPage, viewCnt, pageNaviSize, totalCount);
		
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pageInfo", pageInfo);
		paramMap.put("memberId", memberId);
		
		//차량 목록, 차량 이미지
		ArrayList<Car> carList = dao.selectCarList(paramMap);
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("carList", carList);
		resultMap.put("pageInfo", pageInfo);
		
		return resultMap;
	}

	public ArrayList<Car> selectAllCarList(String memberId) {
		//차량 목록, 차량 이미지
		ArrayList<Car> carList = dao.selectAllCarList(memberId);
		return carList;
	}
	
	public FastApiRes insertEsimateHist(String carId, MultipartFile[] brokenFiles) {
		//예상 수리비 견적 모델 호출(FastAPI)
		
		try {
			// 여러개 파일 body에 추가
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			
			//파일 번호 시작 및 마지막 값, 차량ID 저장 Map
			HashMap<String, String> paramMap = new HashMap<>();
			paramMap.put("carId", carId);
			
			//Spring boot -> FastAPI에, 서버에 저장된 파일명(20251114101331906_00123.png) 전달할 리스트
			ArrayList<String> filePathList = new ArrayList<>();

			
			System.out.println("brokenFiles : " + brokenFiles.length);
			//파손 이미지 갯수만큼, Spring 서버 업로드 -> DB(tbl_broken_file) INSERT -> FastAPI 전달할 body에 추가
			for(int i=0; i<brokenFiles.length; i++) {
				
				MultipartFile uploadBrokenFile = brokenFiles[i];
				String filePath = "";
				
				try {
					//파손 이미지 업로드
					filePath = fileUtil.uploadFile(uploadBrokenFile, "/car/broken/");
					
					//리스트에 서버 파일명 추가
					filePathList.add(filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				
				//이미지 파일 번호
				String brokenFileNo = dao.getBrokenFileNo();
				
				//시작 또는 마지막
				if(i == 0) {
					paramMap.put("brokenFileMin", brokenFileNo);
					paramMap.put("brokenFileMax", brokenFileNo);
				}else if(brokenFiles.length-1 == i) {
					paramMap.put("brokenFileMax", brokenFileNo);
				}
				
				BrokenFile brokenFile = new BrokenFile(brokenFileNo, carId, uploadBrokenFile.getOriginalFilename(), filePath);
				
				dao.insertBrokenFile(brokenFile);
				
				
				
			    Resource resource = new ByteArrayResource(uploadBrokenFile.getBytes()) {
			        @Override
			        public String getFilename() {
			            return uploadBrokenFile.getOriginalFilename();
			        }
			    };
			    body.add("files", resource);   // ← key 이름을 files 로!
			}
			
			
			body.add("filePathList", filePathList);
	
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	
	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
	        
	        //응답 형식 Model 클래스 생성할 것
	        FastApiRes response = restTemplate.postForObject(endpoint+"/mycar/estimate", requestEntity, FastApiRes.class);
	        
	        if(response != null) {
	        	//견적 이력 저장
	        	if(dao.insertEsimateHist(paramMap) > 0) {
	        		return response;
	        	}
	        }
	        
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	public HashMap<String, Object> selectEstiMateList(int reqPage, String memberId) {
		int viewCnt = 10;							//한 페이지당 게시물 수
		int pageNaviSize = 5;						//페이지 네비게이션 길이
		int totalCount = dao.selectEstiMateCount(memberId);	//전체 게시글 수
		
		//페이징 정보
		PageInfo pageInfo = pageUtil.getPageInfo(reqPage, viewCnt, pageNaviSize, totalCount);
		
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pageInfo", pageInfo);
		paramMap.put("memberId", memberId);
		
		ArrayList<EstiMate> estiMateList = dao.selectEstiMateList(paramMap);
		
		//각 이력별 업로드한 이미지들 조회
		for(int i=0; i<estiMateList.size(); i++) {
			EstiMate estiMate = estiMateList.get(i);
			
			paramMap.put("carId", estiMate.getCarId());
			paramMap.put("brokenFileMin", estiMate.getBrokenFileMin());
			paramMap.put("brokenFileMax", estiMate.getBrokenFileMax());
			
			ArrayList<BrokenFile> brokenFileList = dao.selectBrokenFileList(paramMap);
			estiMate.setBrokenFileList(brokenFileList);
		}
		
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("estimateList", estiMateList);
		resultMap.put("pageInfo", pageInfo);
		
		return resultMap;
	}

	
}
