package com.makjan.sulgilddara.liquor.model.vo;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LiquorImage {

	private Integer fileNo;
	private String fileName;
	private String fileRename;
	private String filePath;
	private Integer liquorId;
	
	private MultipartFile uploadFile;
}
