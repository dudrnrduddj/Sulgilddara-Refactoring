package com.makjan.sulgilddara.board.model.vo;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoardReplyUser {
	private String userId;
	private String userName;
	private String kakaoProfile;
	private String userFileRename;
	private String userFilePath;
	
	private Integer boardNo;	
	private Integer replyNo;
	private String replyContent;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/MM/dd HH:mm:ss", timezone="Asia/seoul")
	private Timestamp rCreateDate;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/MM/dd HH:mm:ss", timezone="Asia/seoul")
	private Timestamp rUpdateDate;
}
