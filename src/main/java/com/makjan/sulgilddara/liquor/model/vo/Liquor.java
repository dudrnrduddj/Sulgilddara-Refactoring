package com.makjan.sulgilddara.liquor.model.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Liquor {
	
	private Integer liquorId;
	private String liquorName;
	private float alcholContent;
	private String liquorType ;
	private Integer liquorPrice ;
	private Integer liquorCapacity;
	private Timestamp lCreateDate;
	private Timestamp lUpdateDate;
	private Integer breweryId;
}
