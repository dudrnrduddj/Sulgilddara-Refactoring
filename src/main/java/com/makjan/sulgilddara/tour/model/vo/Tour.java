package com.makjan.sulgilddara.tour.model.vo;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Tour {
	private Integer tourNo;
	private String tourName;
	private String tourContent;
	private String timeTaken;
	private Integer tourPrice;
	
	private String fileName;
	private String fileRename;
	private String filePath;
	private String imagePath;
	
	//brewery
	private Integer breweryNo;
	private String breweryName;
	private String breweryAddr;
	private String breweryPhone;
	private MultipartFile uploadFile;
}
