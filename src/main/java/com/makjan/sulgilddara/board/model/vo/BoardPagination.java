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
public class BoardPagination {
	private int currentPage;
	private int totalCount;
	
	private int boardLimit = 8;
	private int naviLimit = 10;
	
	private int maxPage;
	
	private int startNavi;
	private int endNavi;
	
	private int prevPage;
	private int nextPage;
	
	public BoardPagination(int totalCount, int currentPage) {
		this.totalCount = totalCount;
		this.currentPage = currentPage;
		makePagination();
	}
	
	private void makePagination() {
		if(totalCount > 0) {
			maxPage = (int) Math.ceil((double) totalCount / boardLimit);
		}
		
		startNavi = ((currentPage-1)/naviLimit)*naviLimit + 1;
		endNavi = (startNavi-1) + naviLimit;
		if(totalCount > 0) {
			endNavi = (endNavi > maxPage) ? maxPage : endNavi;			
		}
	
		if(currentPage <= naviLimit) prevPage = 1;
		else prevPage = startNavi - 1;
		
		nextPage = endNavi + 1;	

		if(totalCount > 0) {
			if(endNavi == maxPage) nextPage = maxPage;
		}
	
	}	
	}
    
