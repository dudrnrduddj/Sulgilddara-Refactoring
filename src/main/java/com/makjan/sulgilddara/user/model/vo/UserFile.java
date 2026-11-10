package com.makjan.sulgilddara.user.model.vo;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserFile {

	private int fileNo;
	private String fileName;
	private String fileRename;
	private String filePath;
	private String userId;
	
	private MultipartFile uploadFile;
}
