package com.academy.msai.mycar.model.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

import com.academy.msai.common.model.dto.PageInfo;
import com.academy.msai.mycar.model.dto.BrokenFile;
import com.academy.msai.mycar.model.dto.Car;
import com.academy.msai.mycar.model.dto.EstiMate;

@Mapper
public interface MycarDao {
	
	String getCarId();

	int insertCar(Car car);

	int selectCarCount(String memberId);

	ArrayList<Car> selectCarList(HashMap<String, Object> paramMap);

	ArrayList<Car> selectAllCarList(String memberId);

	String getBrokenFileNo();

	int insertBrokenFile(BrokenFile brokenFile);

	int insertEsimateHist(HashMap<String, String> paramMap);

	int selectEstiMateCount(String memberId);

	ArrayList<EstiMate> selectEstiMateList(HashMap<String, Object> paramMap);

	ArrayList<BrokenFile> selectBrokenFileList(HashMap<String, Object> paramMap);
}
