package com.makjan.sulgilddara.model.vo;

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
public class Pagination {
	private int currentPage;
	private int totalCount;
	
	private int boardLimit = 10;
	private int naviLimit = 10;
	
	private int maxPage;
	
	private int startNavi;
	private int endNavi;
	
	private int prevPage;
	private int nextPage;
	
	public Pagination(int totalCount, int currentPage) {
		this.totalCount = totalCount;
		this.currentPage = currentPage;
		makePagination();
	}
	
	private void makePagination() {
		maxPage = (int) Math.ceil((double) totalCount / boardLimit);
	
		startNavi = ((currentPage-1)/naviLimit)*naviLimit + 1;
	
		endNavi = (startNavi-1) + naviLimit;
	
		endNavi = (endNavi > maxPage) ? maxPage : endNavi;
	
		if(currentPage <= 10) prevPage = 1;
		else prevPage = startNavi - 1;
	
		if(endNavi == maxPage) nextPage = maxPage;
		else nextPage = endNavi + 1;
	
	    // 로그 출력
	    System.out.println("Pagination Info: maxPage=" + maxPage + 
	                       ", startNavi=" + startNavi + 
	                       ", endNavi=" + endNavi + 
	                       ", prevPage=" + prevPage + 
	                       ", nextPage=" + nextPage +
	                       ", totalCount="+ totalCount
	                       );
	}	
	}
    
