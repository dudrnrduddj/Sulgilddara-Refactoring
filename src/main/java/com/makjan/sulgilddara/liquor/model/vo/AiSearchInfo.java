package com.makjan.sulgilddara.liquor.model.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AiSearchInfo {

	private int liquorId;
	private String liquorName;
	private float alcholContent;
	private int liquorPrice;
	private String liquorType;
	private String liquorCapacity;
	private String tags;
}
