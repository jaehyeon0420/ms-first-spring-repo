package com.academy.msai.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageInfo {
	private int start;
	private int end;
	private int pageNo;
	private int pageNaviSize;
	private int totalPage;
}
