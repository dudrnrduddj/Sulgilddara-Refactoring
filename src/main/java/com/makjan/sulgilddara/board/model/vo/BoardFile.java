package com.makjan.sulgilddara.board.model.vo;


import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardFile {
	private Integer boardFileNo;
	private String boardFileName;
	private String boardFileRename;
	private String boardFilePath;
	private Integer boardNo;
	private String tempId;
	private String isThumbnail;
	private String fileOwner;
	private Timestamp createdAt;
	
	public BoardFile() {}
	
	public BoardFile(String boardFileName, String boardFileRename, String boardFilePath, Integer boardNo, String tempId, String isThumbnail, String fileOwner) {
		this.boardFileName = boardFileName;
		this.boardFileRename = boardFileRename;
		this.boardFilePath = boardFilePath;
		this.boardNo = boardNo;
		this.tempId = tempId;
		this.isThumbnail = isThumbnail;
		this.fileOwner = fileOwner;
	}
	
	
}
