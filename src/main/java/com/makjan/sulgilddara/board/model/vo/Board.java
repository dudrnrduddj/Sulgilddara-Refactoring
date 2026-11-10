package com.makjan.sulgilddara.board.model.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Board {
	private Integer boardNo;
	private String boardSubject;
	private String boardContent;
	private String boardWriter;
	private Timestamp boardDate;
	private Timestamp updateDate;
	private Integer viewCount;
	private Double boardPoint;
	private Integer liquorId;
	private String liquorName;
	private String userId;
	
	private BoardFile boardFile;
	
}
