package com.makjan.sulgilddara.board.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordPagination {
	private int currentPage;
	
	private int boardLimit = 8;
	private int naviLimit = 10;
	
	private int startNavi;
	private int endNavi;
	
	private int prevPage;
	private int nextPage;
	
	public KeywordPagination(int currentPage) {
		this.currentPage = currentPage;
		makePagination();
	}
	
	private void makePagination() {
		startNavi = ((currentPage-1)/naviLimit)*naviLimit + 1;
		endNavi = (startNavi-1) + naviLimit;
	
		if(currentPage <= naviLimit) prevPage = 1;
		else prevPage = startNavi - 1;
	
		nextPage = endNavi + 1;
	
	}	
}
