package com.makjan.sulgilddara.board.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class BoardTag {
	private Integer boardTagNo;
	private String boardTagName;
	private Integer boardNo;
}
