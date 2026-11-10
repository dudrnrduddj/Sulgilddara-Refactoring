package com.makjan.sulgilddara.board.model.dto;

import java.util.List;

import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardTag;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardCardDto {
	private final Board board;
	private final List<BoardTag> tags;
	private final List<BoardFile> files;
}
