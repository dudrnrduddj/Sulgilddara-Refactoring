package com.makjan.sulgilddara.board.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchLiquor {
	private Integer liquorId;
	private String liquorName;
	private String liquorType;
	private String filePath;
	private String fileRename;
}
