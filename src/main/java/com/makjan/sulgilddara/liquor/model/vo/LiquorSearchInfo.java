package com.makjan.sulgilddara.liquor.model.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LiquorSearchInfo {
	private String keyword;
	private String keywordType;
	private String breweryLocal;
	private String liquorType;
	private Integer alcholMin;
	private Integer alcholMax;
	private Integer capacityMin;
	private Integer capacityMax;
	private Integer priceMin;
	private Integer priceMax;
	private String tags;
}
