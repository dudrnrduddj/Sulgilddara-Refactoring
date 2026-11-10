package com.makjan.sulgilddara.board.model.dto;

import java.util.List;

public record BoardSearchResult (
	List<Integer> boardNos,
	long totalCount,
	long tookMs,
	boolean totalCountIsLowerBound
	) {
}
