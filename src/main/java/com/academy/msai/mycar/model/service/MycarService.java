package com.academy.msai.mycar.model.service;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	@Value("${file.uploadPath}")
	private String uploadPath;

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
	
	public List<FastApiRes> insertEsimateHist(String carId, MultipartFile[] brokenFiles) {
		//예상 수리비 견적 모델 호출(FastAPI)
		
		try {
			// 여러개 파일 body에 추가
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			
			//파일 번호 시작 및 마지막 값, 차량ID 저장 Map
			HashMap<String, String> paramMap = new HashMap<>();
			paramMap.put("carId", carId);
			
			//Spring boot -> FastAPI에, 서버에 저장된 파일명(20251114101331906_00123.png) 전달할 리스트
			ArrayList<String> filePathList = new ArrayList<>();

			
			
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
				
				//DB 저장을 위한, 시작 또는 마지막 파일 번호
				if(i == 0) {
					paramMap.put("brokenFileMin", brokenFileNo);
					paramMap.put("brokenFileMax", brokenFileNo);
				}else if(brokenFiles.length-1 == i) {
					paramMap.put("brokenFileMax", brokenFileNo);
				}
				
				
				//tbl_borken_file - Insert
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
	        
	        /*
	        ResponseEntity<List<FastApiRes>> response = restTemplate.exchange(
	        		endpoint+"/mycar/estimate",
	                org.springframework.http.HttpMethod.POST,
	                requestEntity,
	                new ParameterizedTypeReference<List<FastApiRes>>() {}
	            );
	        */
	        ResponseEntity<byte[]> response = restTemplate.exchange(
	        		endpoint+"/mycar/estimate",
	        		org.springframework.http.HttpMethod.POST,
	        		requestEntity,
	                byte[].class
	        );
	        byte[] zipBytes = response.getBody();

	        // ZIP 파일 Parse
	        ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
	        ZipInputStream zis = new ZipInputStream(bis);
	        ZipEntry entry;
	        
	        List<FastApiRes> fastApiList = null;
	        while ((entry = zis.getNextEntry()) != null) {
	        	
	        	// Json List
	            if (entry.getName().equals("result.json")) {
	                String jsonStr = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
	                
	                //db에 Text로 저장
	                paramMap.put("jsonStr", jsonStr);
	                
	                ObjectMapper mapper = new ObjectMapper();
	                fastApiList  = mapper.readValue(
	                        jsonStr,
	                        new TypeReference<List<FastApiRes>>() {}
	                );

	            }
	            
	            // jpg
	            if (entry.getName().endsWith("image.jpg")) {
	                FileOutputStream fos = new FileOutputStream(uploadPath + "/car/broken/result/" + entry.getName());
	                fos.write(zis.readAllBytes());
	                fos.close();
	            }
	        }

	        zis.close();     
	        
	        if(response != null) {
	        	//tbl_estimate - Insert
	        	if(dao.insertEsimateHist(paramMap) > 0) {
	        		return fastApiList ;
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
